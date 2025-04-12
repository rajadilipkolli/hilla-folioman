package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.portfolio.entities.FolioScheme;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserPortfolioValue;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.TransactionType;
import com.app.folioman.portfolio.repository.FolioSchemeRepository;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.app.folioman.portfolio.util.XirrCalculator;
import com.app.folioman.shared.LocalDateUtility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class PortfolioValueUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioValueUpdateService.class);
    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final MFNavService mfNavService;
    private final FolioSchemeRepository folioSchemeRepository;

    // Add a constant for excluded transaction types
    private static final EnumSet<TransactionType> TAX_TRANSACTION_TYPES = EnumSet.of(
            TransactionType.STAMP_DUTY_TAX, TransactionType.TDS_TAX, TransactionType.STT_TAX, TransactionType.MISC);

    // Add a constant for redemption transaction types
    private static final EnumSet<TransactionType> REDEMPTION_TRANSACTION_TYPES =
            EnumSet.of(TransactionType.REDEMPTION, TransactionType.SWITCH_OUT);

    public PortfolioValueUpdateService(
            UserPortfolioValueRepository userPortfolioValueRepository,
            MFNavService mfNavService,
            FolioSchemeRepository folioSchemeRepository) {
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.mfNavService = mfNavService;
        this.folioSchemeRepository = folioSchemeRepository;
    }

    @Async
    public void updatePortfolioValue(UserCASDetails userCASDetails) {
        handleDailyPortFolioValueUpdate(userCASDetails);
    }

    private void handleDailyPortFolioValueUpdate(UserCASDetails userCASDetails) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info(
                "Starting portfolio value update for CAS ID: {}, PAN: {}",
                userCASDetails.getId(),
                userCASDetails.getFolios().getFirst().getPan());

        try {
            // Step 1: Collect all transactions from the folios and schemes
            log.debug("Collecting and filtering transactions for CAS ID: {}", userCASDetails.getId());
            List<UserTransactionDetails> transactionList = userCASDetails.getFolios().stream()
                    .flatMap(folio -> folio.getSchemes().stream())
                    .flatMap(scheme -> scheme.getTransactions().stream())
                    .filter(transaction -> !TAX_TRANSACTION_TYPES.contains(transaction.getType()))
                    .sorted(Comparator.comparing(UserTransactionDetails::getTransactionDate))
                    .toList();

            if (transactionList.isEmpty()) {
                log.warn("No relevant transactions found for CAS ID: {}", userCASDetails.getId());
                return;
            }

            log.info("Found {} relevant transactions for CAS ID: {}", transactionList.size(), userCASDetails.getId());

            calculateAndInsertDailyPortfolioValues(transactionList, userCASDetails);

            stopWatch.stop();
            log.info(
                    "Completed portfolio value update for CAS ID: {}, took {} seconds",
                    userCASDetails.getId(),
                    stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            log.error("Error updating portfolio values for CAS ID: {}", userCASDetails.getId(), e);
        }
    }

    private void calculateAndInsertDailyPortfolioValues(
            List<UserTransactionDetails> transactionList, UserCASDetails userCASDetails) {

        StopWatch methodStartTime = new StopWatch();
        methodStartTime.start();

        // Step 2: Prepare data structures
        LocalDate startDate = transactionList.getFirst().getTransactionDate();
        LocalDate endDate = LocalDateUtility.getYesterday();

        log.debug(
                "Calculating portfolio values for CAS ID: {} from {} to {}",
                userCASDetails.getId(),
                startDate,
                endDate);

        // Step 3: Group transactions by date for efficient lookup
        Map<LocalDate, List<UserTransactionDetails>> transactionsByDate = transactionList.stream()
                .collect(Collectors.groupingBy(
                        UserTransactionDetails::getTransactionDate, TreeMap::new, Collectors.toList()));

        // Step 4: Pre-fetch NAVs in bulk for all schemes and dates in one go
        Set<Long> schemeCodes = transactionList.stream()
                .map(transaction -> transaction.getUserSchemeDetails().getAmfi())
                .collect(Collectors.toSet());

        log.debug("Fetching NAVs for {} schemes for CAS ID: {}", schemeCodes.size(), userCASDetails.getId());
        StopWatch navFetchStart = new StopWatch();
        navFetchStart.start();

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate =
                mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

        navFetchStart.stop();
        log.debug(
                "NAV fetching completed in {} ms for CAS ID: {}",
                navFetchStart.getTotalTimeMillis(),
                userCASDetails.getId());

        // Step 5: Pre-compute the total cumulative invested amount and units for each scheme
        Map<Long, BigDecimal> cumulativeInvestedAmountByScheme = new HashMap<>();
        Map<Long, Double> cumulativeUnitsByScheme = new HashMap<>();

        // Step 6: Process each day in the date range
        List<UserPortfolioValue> portfolioValueEntityList = new ArrayList<>();

        // For XIRR calculation - using Maps instead of Lists for the new XirrCalculator implementation
        Map<LocalDate, BigDecimal> allCashFlows = new HashMap<>();

        // Track cash flows for XIRR calculation using Maps for each scheme
        Map<Long, Map<LocalDate, BigDecimal>> cashFlowsByScheme = new HashMap<>();

        startDate.datesUntil(endDate.plusDays(1)).forEach(currentDate -> {
            BigDecimal totalPortfolioValue = BigDecimal.ZERO;

            // Step 7: Update invested amount and units if transactions are present on this date
            List<UserTransactionDetails> dailyTransactions =
                    transactionsByDate.getOrDefault(currentDate, Collections.emptyList());
            dailyTransactions.forEach(transaction -> {
                Long amfiCode = transaction.getUserSchemeDetails().getAmfi();
                BigDecimal transactionAmount = transaction.getAmount();
                Double transactionUnits = transaction.getUnits();
                if (transactionAmount == null) {
                    // happens when type is purchase and additional allotment
                    transactionAmount = BigDecimal.valueOf(0.0001);
                }
                if (transactionUnits == null) {
                    // happens when transaction type is dividend payout
                    transactionUnits = 0.0;
                }

                // For XIRR calculation - initialize the map for this scheme if needed
                if (!cashFlowsByScheme.containsKey(amfiCode)) {
                    cashFlowsByScheme.put(amfiCode, new HashMap<>());
                }

                // Record cash flows for all transactions except taxes
                TransactionType transactionType = transaction.getType();
                if (transactionType != null && !TAX_TRANSACTION_TYPES.contains(transactionType)) {
                    // Investment is negative cash flow, redemption is positive
                    BigDecimal cashFlowAmount = REDEMPTION_TRANSACTION_TYPES.contains(transactionType)
                            ? transactionAmount // Positive (money received)
                            : transactionAmount.negate(); // Negative (money invested)

                    // Add to scheme-specific cash flows (merge values for same date)
                    cashFlowsByScheme
                            .get(amfiCode)
                            .merge(transaction.getTransactionDate(), cashFlowAmount, BigDecimal::add);

                    // Also add to the all cash flows map for overall XIRR (merge values for same date)
                    allCashFlows.merge(transaction.getTransactionDate(), cashFlowAmount, BigDecimal::add);
                }

                // Update cumulative invested amount and units for the scheme
                cumulativeInvestedAmountByScheme.merge(amfiCode, transactionAmount, BigDecimal::add);
                cumulativeUnitsByScheme.merge(amfiCode, transactionUnits, Double::sum);
            });

            // Step 8: Calculate the portfolio value by fetching NAVs in bulk for the day
            LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(currentDate);
            for (Long schemeCode : cumulativeUnitsByScheme.keySet()) {
                int attempts = 0;
                int maxAttempts = 5;
                MFSchemeNavProjection navOnCurrentDate = null;

                // First check if we have NAV data for this scheme
                if (navsBySchemeAndDate.containsKey(schemeCode)) {
                    navOnCurrentDate = navsBySchemeAndDate.get(schemeCode).get(adjustedDate);
                    while (navOnCurrentDate == null && attempts <= maxAttempts) {
                        adjustedDate = LocalDateUtility.getAdjustedDate(adjustedDate.minusDays(1));
                        navOnCurrentDate = navsBySchemeAndDate.get(schemeCode).get(adjustedDate);
                        attempts++;
                    }
                }

                if (navOnCurrentDate != null) {
                    BigDecimal navValue = navOnCurrentDate.nav();
                    double units = cumulativeUnitsByScheme.get(schemeCode);
                    totalPortfolioValue = totalPortfolioValue.add(navValue.multiply(BigDecimal.valueOf(units)));
                } else {
                    log.warn(
                            "NAV not found for scheme {} on date {} after {} attempts - continuing with other schemes",
                            schemeCode,
                            currentDate,
                            maxAttempts);
                    // Continue with other schemes instead of stopping the entire process
                }
            }

            // Step 9: Sum total invested amount
            BigDecimal totalInvestedAmount =
                    cumulativeInvestedAmountByScheme.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            // Step 10: Insert into UserPortfolioValue
            UserPortfolioValue portfolioValueEntity = new UserPortfolioValue();
            portfolioValueEntity.setDate(currentDate);
            portfolioValueEntity.setInvested(totalInvestedAmount);
            portfolioValueEntity.setValue(totalPortfolioValue);
            portfolioValueEntity.setUserCasDetails(userCASDetails);
            portfolioValueEntityList.add(portfolioValueEntity);

            // For the last date (most recent), we need to add the portfolio value as a positive cash flow
            if (currentDate.equals(endDate)) {
                // Add current valuations for each scheme to the scheme-specific cash flows
                for (Long schemeCode : cumulativeUnitsByScheme.keySet()) {
                    Double units = cumulativeUnitsByScheme.get(schemeCode);
                    Map<LocalDate, MFSchemeNavProjection> localDateMFSchemeNavProjectionMap =
                            navsBySchemeAndDate.get(schemeCode);
                    if (localDateMFSchemeNavProjectionMap != null) {
                        MFSchemeNavProjection navOnCurrentDate = localDateMFSchemeNavProjectionMap.get(adjustedDate);

                        if (navOnCurrentDate != null && units > 0) {
                            BigDecimal schemeValue = navOnCurrentDate.nav().multiply(BigDecimal.valueOf(units));

                            // Add current valuation as positive cash flow for XIRR calculation
                            if (cashFlowsByScheme.containsKey(schemeCode)) {
                                cashFlowsByScheme.get(schemeCode).merge(currentDate, schemeValue, BigDecimal::add);
                            }
                        }
                    } else {
                        log.warn(
                                "NAV not found for scheme {} on date {} - skipping cash flow calculation",
                                schemeCode,
                                currentDate);
                    }
                }

                // Add total portfolio value as positive cash flow for overall XIRR
                allCashFlows.put(currentDate, totalPortfolioValue);
            }
        });

        // Step 11: Calculate XIRR for each scheme and save to FolioScheme entities
        for (UserTransactionDetails transaction : transactionList) {
            Long schemeCode = transaction.getUserSchemeDetails().getAmfi();
            Long schemeDetailId = transaction.getUserSchemeDetails().getId();

            // Only calculate XIRR if we have sufficient cash flows
            if (cashFlowsByScheme.containsKey(schemeCode)
                    && cashFlowsByScheme.get(schemeCode).size() >= 2) { // Need at least 2 cash flows for XIRR
                try {
                    // Use the new XirrCalculator.xirr method with Map parameter
                    BigDecimal xirrValue = XirrCalculator.xirr(cashFlowsByScheme.get(schemeCode));

                    // Create or update FolioScheme with XIRR
                    FolioScheme folioScheme =
                            findOrCreateFolioScheme(schemeDetailId, transaction.getUserSchemeDetails());
                    folioScheme.setXirr(xirrValue);
                    folioScheme.setValuationDate(endDate);
                    folioSchemeRepository.save(folioScheme);

                    log.debug("Saved XIRR {} for scheme ID {}", xirrValue, schemeDetailId);
                } catch (Exception e) {
                    log.warn("Unable to calculate XIRR for scheme ID {}: {}", schemeDetailId, e.getMessage());
                }
            }
        }

        // Step 12: Try to calculate overall portfolio XIRR
        try {
            if (allCashFlows.size() >= 2) { // Need at least 2 cash flows for meaningful XIRR
                // Use the new XirrCalculator.xirr method with Map parameter
                BigDecimal overallXirr = XirrCalculator.xirr(allCashFlows);

                // Set XIRR on the most recent portfolio value entity
                UserPortfolioValue mostRecent = portfolioValueEntityList.getLast();
                mostRecent.setXirr(overallXirr);
                log.debug("Overall portfolio XIRR calculated: {}", overallXirr);
            }
        } catch (Exception e) {
            log.warn("Unable to calculate overall portfolio XIRR: {}", e.getMessage());
        }

        userPortfolioValueRepository.saveAll(portfolioValueEntityList);

        methodStartTime.stop();
        log.debug("Portfolio values calculated and inserted, took {} seconds", methodStartTime.getTotalTimeSeconds());
    }

    /**
     * Find an existing FolioScheme entity or create a new one if it doesn't exist.
     * Enhanced with better tracking for asynchronous operation.
     *
     * @param schemeDetailId ID of the scheme to find
     * @param userSchemeDetails The user scheme details entity
     * @return The found or newly created FolioScheme
     */
    private FolioScheme findOrCreateFolioScheme(Long schemeDetailId, UserSchemeDetails userSchemeDetails) {
        String operationId = generateOperationId(schemeDetailId);
        log.debug("[{}] Looking up FolioScheme for schemeDetailId: {}", operationId, schemeDetailId);

        try {
            FolioScheme folioScheme = findExistingFolioScheme(schemeDetailId, operationId);
            return folioScheme != null
                    ? folioScheme
                    : createNewFolioScheme(schemeDetailId, userSchemeDetails, operationId);
        } catch (Exception e) {
            handleFolioSchemeError(operationId, schemeDetailId, e);
            throw new RuntimeException("Failed to process FolioScheme for scheme ID: " + schemeDetailId, e);
        }
    }

    private String generateOperationId(Long schemeDetailId) {
        return "folioScheme-" + schemeDetailId + "-" + System.currentTimeMillis();
    }

    private FolioScheme findExistingFolioScheme(Long schemeDetailId, String operationId) {
        try {
            FolioScheme folioScheme = folioSchemeRepository.findByUserSchemeDetails_Id(schemeDetailId);
            if (folioScheme != null) {
                log.debug("[{}] Found existing FolioScheme for schemeDetailId: {}", operationId, schemeDetailId);
            }
            return folioScheme;
        } catch (Exception e) {
            log.error("[{}] Error while finding FolioScheme for schemeDetailId: {}", operationId, schemeDetailId, e);
            throw e;
        }
    }

    private FolioScheme createNewFolioScheme(
            Long schemeDetailId, UserSchemeDetails userSchemeDetails, String operationId) {
        log.debug(
                "[{}] No existing FolioScheme found, creating new one for schemeDetailId: {}",
                operationId,
                schemeDetailId);
        FolioScheme folioScheme = new FolioScheme();
        folioScheme.setUserSchemeDetails(userSchemeDetails);
        folioScheme.setUserFolioDetails(userSchemeDetails.getUserFolioDetails());
        log.debug("[{}] New FolioScheme created for schemeDetailId: {}", operationId, schemeDetailId);
        return folioScheme;
    }

    private void handleFolioSchemeError(String operationId, Long schemeDetailId, Exception e) {
        log.error(
                "[{}] Error while finding/creating FolioScheme for schemeDetailId: {}", operationId, schemeDetailId, e);
    }
}
