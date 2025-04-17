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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class PortfolioValueUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioValueUpdateService.class);

    // Add a constant for excluded transaction types
    private static final EnumSet<TransactionType> TAX_TRANSACTION_TYPES = EnumSet.of(
            TransactionType.STAMP_DUTY_TAX, TransactionType.TDS_TAX, TransactionType.STT_TAX, TransactionType.MISC);

    // Add a constant for redemption transaction types
    private static final EnumSet<TransactionType> REDEMPTION_TRANSACTION_TYPES =
            EnumSet.of(TransactionType.REDEMPTION, TransactionType.SWITCH_OUT);

    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final MFNavService mfNavService;
    private final FolioSchemeRepository folioSchemeRepository;

    PortfolioValueUpdateService(
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
            List<UserTransactionDetails> transactionList = collectRelevantTransactions(userCASDetails);

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

    private List<UserTransactionDetails> collectRelevantTransactions(UserCASDetails userCASDetails) {
        log.debug("Collecting and filtering transactions for CAS ID: {}", userCASDetails.getId());
        List<UserTransactionDetails> transactionList = userCASDetails.getFolios().stream()
                .flatMap(folio -> folio.getSchemes().stream())
                .flatMap(scheme -> scheme.getTransactions().stream())
                .filter(transaction -> !TAX_TRANSACTION_TYPES.contains(transaction.getType()))
                .sorted(Comparator.comparing(UserTransactionDetails::getTransactionDate))
                .toList();

        log.info("Found {} relevant transactions for CAS ID: {}", transactionList.size(), userCASDetails.getId());
        return transactionList;
    }

    private void calculateAndInsertDailyPortfolioValues(
            List<UserTransactionDetails> transactionList, UserCASDetails userCASDetails) {

        StopWatch methodStartTime = new StopWatch();
        methodStartTime.start();

        // Prepare data and date range
        LocalDate startDate = transactionList.getFirst().getTransactionDate();
        LocalDate endDate = LocalDateUtility.getYesterday();

        log.debug(
                "Calculating portfolio values for CAS ID: {} from {} to {}",
                userCASDetails.getId(),
                startDate,
                endDate);

        // Prepare data structures
        Map<LocalDate, List<UserTransactionDetails>> transactionsByDate = groupTransactionsByDate(transactionList);
        Set<Long> schemeCodes = extractSchemeCodes(transactionList);
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate =
                fetchNavData(schemeCodes, startDate, endDate, userCASDetails);

        // Process portfolio data
        PortfolioDataContainer dataContainer =
                new PortfolioDataContainer(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        List<UserPortfolioValue> portfolioValueEntityList = processDailyPortfolioValues(
                startDate, endDate, transactionsByDate, navsBySchemeAndDate, dataContainer, userCASDetails);

        // Calculate and save XIRRs
        calculateAndSaveSchemeXirrs(transactionList, dataContainer, endDate);
        calculateAndSavePortfolioXirr(portfolioValueEntityList, dataContainer.allCashFlows());

        // Save portfolio values
        userPortfolioValueRepository.saveAll(portfolioValueEntityList);

        methodStartTime.stop();
        log.debug("Portfolio values calculated and inserted, took {} seconds", methodStartTime.getTotalTimeSeconds());
    }

    private Map<LocalDate, List<UserTransactionDetails>> groupTransactionsByDate(
            List<UserTransactionDetails> transactionList) {
        return transactionList.stream()
                .collect(Collectors.groupingBy(
                        UserTransactionDetails::getTransactionDate, TreeMap::new, Collectors.toList()));
    }

    private Set<Long> extractSchemeCodes(List<UserTransactionDetails> transactionList) {
        return transactionList.stream()
                .map(transaction -> transaction.getUserSchemeDetails().getAmfi())
                .collect(Collectors.toSet());
    }

    private Map<Long, Map<LocalDate, MFSchemeNavProjection>> fetchNavData(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate, UserCASDetails userCASDetails) {

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

        return navsBySchemeAndDate;
    }

    // Container record to hold portfolio data during processing
    record PortfolioDataContainer(
            Map<Long, BigDecimal> cumulativeInvestedAmountByScheme,
            Map<Long, Double> cumulativeUnitsByScheme,
            Map<LocalDate, BigDecimal> allCashFlows,
            Map<Long, Map<LocalDate, BigDecimal>> cashFlowsByScheme) {}

    private List<UserPortfolioValue> processDailyPortfolioValues(
            LocalDate startDate,
            LocalDate endDate,
            Map<LocalDate, List<UserTransactionDetails>> transactionsByDate,
            Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate,
            PortfolioDataContainer dataContainer,
            UserCASDetails userCASDetails) {

        List<UserPortfolioValue> portfolioValueEntityList = new ArrayList<>();

        startDate.datesUntil(endDate.plusDays(1)).forEach(currentDate -> {
            // Process transactions for the current date
            processTransactionsForDate(currentDate, transactionsByDate, dataContainer);

            // Calculate portfolio value for the current date
            BigDecimal totalPortfolioValue = calculatePortfolioValueForDate(
                    currentDate, dataContainer.cumulativeUnitsByScheme(), navsBySchemeAndDate);

            // Calculate total invested amount
            BigDecimal totalInvestedAmount = dataContainer.cumulativeInvestedAmountByScheme().values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create portfolio value entity
            UserPortfolioValue portfolioValueEntity =
                    createPortfolioValueEntity(currentDate, totalInvestedAmount, totalPortfolioValue, userCASDetails);
            portfolioValueEntityList.add(portfolioValueEntity);

            // For the last date, add portfolio value as a positive cash flow for XIRR calculation
            if (currentDate.equals(endDate)) {
                addFinalValuationCashFlows(currentDate, dataContainer, navsBySchemeAndDate, totalPortfolioValue);
            }
        });

        return portfolioValueEntityList;
    }

    private void processTransactionsForDate(
            LocalDate currentDate,
            Map<LocalDate, List<UserTransactionDetails>> transactionsByDate,
            PortfolioDataContainer dataContainer) {

        List<UserTransactionDetails> dailyTransactions =
                transactionsByDate.getOrDefault(currentDate, Collections.emptyList());

        dailyTransactions.forEach(transaction -> processTransaction(transaction, dataContainer));
    }

    private void processTransaction(UserTransactionDetails transaction, PortfolioDataContainer dataContainer) {

        Long amfiCode = transaction.getUserSchemeDetails().getAmfi();
        BigDecimal transactionAmount = getTransactionAmount(transaction);
        Double transactionUnits = getTransactionUnits(transaction);

        // Initialize cash flow tracking for this scheme if needed
        dataContainer.cashFlowsByScheme().computeIfAbsent(amfiCode, key -> new HashMap<>());

        recordCashFlows(transaction, amfiCode, transactionAmount, dataContainer);

        // Update cumulative invested amount and units for the scheme
        dataContainer.cumulativeInvestedAmountByScheme().merge(amfiCode, transactionAmount, BigDecimal::add);
        dataContainer.cumulativeUnitsByScheme().merge(amfiCode, transactionUnits, Double::sum);
    }

    private BigDecimal getTransactionAmount(UserTransactionDetails transaction) {
        BigDecimal transactionAmount = transaction.getAmount();
        // happens when type is purchase and additional allotment
        return Objects.requireNonNullElseGet(transactionAmount, () -> BigDecimal.valueOf(0.0001));
    }

    private Double getTransactionUnits(UserTransactionDetails transaction) {
        Double transactionUnits = transaction.getUnits();
        // happens when transaction type is dividend payout
        return Objects.requireNonNullElse(transactionUnits, 0.0);
    }

    private void recordCashFlows(
            UserTransactionDetails transaction,
            Long amfiCode,
            BigDecimal transactionAmount,
            PortfolioDataContainer dataContainer) {

        TransactionType transactionType = transaction.getType();
        if (transactionType != null && !TAX_TRANSACTION_TYPES.contains(transactionType)) {
            // Investment is negative cash flow, redemption is positive
            BigDecimal cashFlowAmount = REDEMPTION_TRANSACTION_TYPES.contains(transactionType)
                    ? transactionAmount // Positive (money received)
                    : transactionAmount.negate(); // Negative (money invested)

            // Add to scheme-specific cash flows (merge values for same date)
            dataContainer
                    .cashFlowsByScheme()
                    .get(amfiCode)
                    .merge(transaction.getTransactionDate(), cashFlowAmount, BigDecimal::add);

            // Also add to the all cash flows map for overall XIRR (merge values for same date)
            dataContainer.allCashFlows().merge(transaction.getTransactionDate(), cashFlowAmount, BigDecimal::add);
        }
    }

    private BigDecimal calculatePortfolioValueForDate(
            LocalDate currentDate,
            Map<Long, Double> cumulativeUnitsByScheme,
            Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate) {

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        for (Long schemeCode : cumulativeUnitsByScheme.keySet()) {
            MFSchemeNavProjection navOnCurrentDate =
                    findNavForSchemeAndDate(schemeCode, currentDate, navsBySchemeAndDate);

            if (navOnCurrentDate != null) {
                BigDecimal navValue = navOnCurrentDate.nav();
                double units = cumulativeUnitsByScheme.get(schemeCode);
                totalPortfolioValue = totalPortfolioValue.add(navValue.multiply(BigDecimal.valueOf(units)));
            }
        }

        return totalPortfolioValue;
    }

    private MFSchemeNavProjection findNavForSchemeAndDate(
            Long schemeCode,
            LocalDate currentDate,
            Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate) {

        if (!navsBySchemeAndDate.containsKey(schemeCode)) {
            return null;
        }

        LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(currentDate);
        int attempts = 0;
        int maxAttempts = 5;
        MFSchemeNavProjection navOnCurrentDate =
                navsBySchemeAndDate.get(schemeCode).get(adjustedDate);

        while (navOnCurrentDate == null && attempts < maxAttempts) {
            attempts++;
            adjustedDate = LocalDateUtility.getAdjustedDate(adjustedDate.minusDays(1));
            navOnCurrentDate = navsBySchemeAndDate.get(schemeCode).get(adjustedDate);
        }

        if (navOnCurrentDate == null) {
            log.warn(
                    "NAV not found for scheme {} on date {} after {} attempts - continuing with other schemes",
                    schemeCode,
                    currentDate,
                    maxAttempts);
        }

        return navOnCurrentDate;
    }

    private UserPortfolioValue createPortfolioValueEntity(
            LocalDate currentDate,
            BigDecimal totalInvestedAmount,
            BigDecimal totalPortfolioValue,
            UserCASDetails userCASDetails) {

        UserPortfolioValue portfolioValueEntity = new UserPortfolioValue();
        portfolioValueEntity.setDate(currentDate);
        portfolioValueEntity.setInvested(totalInvestedAmount);
        portfolioValueEntity.setValue(totalPortfolioValue);
        portfolioValueEntity.setUserCasDetails(userCASDetails);
        return portfolioValueEntity;
    }

    private void addFinalValuationCashFlows(
            LocalDate currentDate,
            PortfolioDataContainer dataContainer,
            Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate,
            BigDecimal totalPortfolioValue) {

        // Add current valuations for each scheme to the scheme-specific cash flows
        for (Long schemeCode : dataContainer.cumulativeUnitsByScheme().keySet()) {
            Double units = dataContainer.cumulativeUnitsByScheme().get(schemeCode);
            if (units <= 0) {
                continue;
            }

            Map<LocalDate, MFSchemeNavProjection> navMap = navsBySchemeAndDate.get(schemeCode);
            if (navMap == null) {
                log.warn(
                        "NAV not found for scheme {} on date {} - skipping cash flow calculation",
                        schemeCode,
                        currentDate);
                continue;
            }

            LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(currentDate);
            MFSchemeNavProjection navOnCurrentDate = navMap.get(adjustedDate);

            if (navOnCurrentDate != null) {
                BigDecimal schemeValue = navOnCurrentDate.nav().multiply(BigDecimal.valueOf(units));

                // Add current valuation as positive cash flow for XIRR calculation
                if (dataContainer.cashFlowsByScheme().containsKey(schemeCode)) {
                    dataContainer.cashFlowsByScheme().get(schemeCode).merge(currentDate, schemeValue, BigDecimal::add);
                }
            }
        }

        // Add total portfolio value as positive cash flow for overall XIRR
        // Use merge instead of put to preserve any existing cash flows on this date
        dataContainer.allCashFlows().merge(currentDate, totalPortfolioValue, BigDecimal::add);
    }

    private void calculateAndSaveSchemeXirrs(
            List<UserTransactionDetails> transactionList, PortfolioDataContainer dataContainer, LocalDate endDate) {

        // Create a set to track processed scheme IDs to avoid duplicate calculations
        Set<Long> processedSchemeIds = new HashSet<>();

        for (UserTransactionDetails transaction : transactionList) {
            Long schemeCode = transaction.getUserSchemeDetails().getAmfi();
            Long schemeDetailId = transaction.getUserSchemeDetails().getId();

            // Skip if already processed
            if (processedSchemeIds.contains(schemeDetailId)) {
                continue;
            }
            processedSchemeIds.add(schemeDetailId);

            // Calculate and save XIRR for this scheme
            calculateAndSaveSchemeXirr(
                    schemeCode, schemeDetailId, dataContainer, endDate, transaction.getUserSchemeDetails());
        }
    }

    private void calculateAndSaveSchemeXirr(
            Long schemeCode,
            Long schemeDetailId,
            PortfolioDataContainer dataContainer,
            LocalDate endDate,
            UserSchemeDetails userSchemeDetails) {

        // Only calculate XIRR if we have sufficient cash flows
        if (!dataContainer.cashFlowsByScheme().containsKey(schemeCode)
                || dataContainer.cashFlowsByScheme().get(schemeCode).size() < 2) {
            return; // Need at least 2 cash flows for XIRR
        }

        try {
            // Use the new XirrCalculator.xirr method with Map parameter
            BigDecimal xirrValue =
                    XirrCalculator.xirr(dataContainer.cashFlowsByScheme().get(schemeCode));

            // Create or update FolioScheme with XIRR
            FolioScheme folioScheme = findOrCreateFolioScheme(schemeDetailId, userSchemeDetails);
            folioScheme.setXirr(xirrValue);
            folioScheme.setValuationDate(endDate);
            folioSchemeRepository.save(folioScheme);

            log.debug("Saved XIRR {} for scheme ID {}", xirrValue, schemeDetailId);
        } catch (Exception e) {
            log.warn("Unable to calculate XIRR for scheme ID {}: {}", schemeDetailId, e.getMessage());
        }
    }

    private void calculateAndSavePortfolioXirr(
            List<UserPortfolioValue> portfolioValueEntityList, Map<LocalDate, BigDecimal> allCashFlows) {

        try {
            if (allCashFlows.size() >= 2) { // Need at least 2 cash flows for meaningful XIRR
                // Use the new XirrCalculator.xirr method with Map parameter
                BigDecimal overallXirr = XirrCalculator.xirr(allCashFlows);

                // Set XIRR on the most recent portfolio value entity
                if (!portfolioValueEntityList.isEmpty()) {
                    UserPortfolioValue mostRecent = portfolioValueEntityList.getLast();
                    mostRecent.setXirr(overallXirr);
                    log.debug("Overall portfolio XIRR calculated: {}", overallXirr);
                }
            }
        } catch (Exception e) {
            log.warn("Unable to calculate overall portfolio XIRR: {}", e.getMessage());
        }
    }

    /**
     * Find an existing FolioScheme entity or create a new one if it doesn't exist.
     */
    private FolioScheme findOrCreateFolioScheme(Long schemeDetailId, UserSchemeDetails userSchemeDetails) {
        String operationId = generateOperationId(schemeDetailId);
        log.debug("[{}] Looking up FolioScheme for schemeDetailId: {}", operationId, schemeDetailId);

        FolioScheme folioScheme = findExistingFolioScheme(schemeDetailId, operationId);
        return folioScheme != null ? folioScheme : createNewFolioScheme(schemeDetailId, userSchemeDetails, operationId);
    }

    private String generateOperationId(Long schemeDetailId) {
        return "folioScheme-" + schemeDetailId + "-" + UUID.randomUUID();
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
}
