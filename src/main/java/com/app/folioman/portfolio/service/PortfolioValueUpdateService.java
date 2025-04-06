package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserPortfolioValue;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.TransactionType;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
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
import java.util.concurrent.atomic.AtomicInteger;
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

    public PortfolioValueUpdateService(
            UserPortfolioValueRepository userPortfolioValueRepository, MFNavService mfNavService) {
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.mfNavService = mfNavService;
    }

    @Async
    public void updatePortfolioValue(UserCASDetails userCASDetails) {
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
                    .filter(transaction -> !EnumSet.of(
                                    TransactionType.STAMP_DUTY_TAX,
                                    TransactionType.TDS_TAX,
                                    TransactionType.STT_TAX,
                                    TransactionType.MISC)
                            .contains(transaction.getType()))
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
        AtomicInteger processedDays = new AtomicInteger();
        AtomicInteger daysWithTransactions = new AtomicInteger();
        AtomicInteger navMissingCount = new AtomicInteger();

        log.debug("Starting day-by-day portfolio value calculation for CAS ID: {}", userCASDetails.getId());
        StopWatch dailyProcessingStart = new StopWatch();
        dailyProcessingStart.start();

        startDate.datesUntil(endDate.plusDays(1)).forEach(currentDate -> {
            processedDays.getAndIncrement();
            BigDecimal totalPortfolioValue = BigDecimal.ZERO;

            // Step 7: Update invested amount and units if transactions are present on this date
            List<UserTransactionDetails> dailyTransactions =
                    transactionsByDate.getOrDefault(currentDate, Collections.emptyList());

            if (!dailyTransactions.isEmpty()) {
                daysWithTransactions.getAndIncrement();
                log.trace(
                        "Processing {} transactions for date {} for CAS ID: {}",
                        dailyTransactions.size(),
                        currentDate,
                        userCASDetails.getId());
            }

            dailyTransactions.forEach(transaction -> {
                Long amfiCode = transaction.getUserSchemeDetails().getAmfi();
                BigDecimal transactionAmount = transaction.getAmount();
                double transactionUnits = transaction.getUnits();

                // Log null transaction amounts which might indicate data issues
                if (transactionAmount == null) {
                    log.debug(
                            "Null transaction amount for transaction ID: {}, type: {}, scheme: {}",
                            transaction.getId(),
                            transaction.getType(),
                            amfiCode);
                    // happens when type is purchase and additional allotment
                    transactionAmount = BigDecimal.valueOf(0.0001);
                }

                // Update cumulative invested amount and units for the scheme
                cumulativeInvestedAmountByScheme.merge(amfiCode, transactionAmount, BigDecimal::add);
                cumulativeUnitsByScheme.merge(amfiCode, transactionUnits, Double::sum);
            });

            // Step 8: Calculate the portfolio value by fetching NAVs in bulk for the day
            LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(currentDate);
            for (Long schemeCode : cumulativeUnitsByScheme.keySet()) {
                int attempts = 0;
                int maxAttempts = 3;
                MFSchemeNavProjection navOnCurrentDate =
                        navsBySchemeAndDate.get(schemeCode).get(adjustedDate);

                // Log each NAV lookup attempt
                if (navOnCurrentDate == null) {
                    log.trace(
                            "NAV not found on first attempt for scheme {} on date {} for CAS ID: {}, trying previous dates",
                            schemeCode,
                            adjustedDate,
                            userCASDetails.getId());
                }

                LocalDate searchDate = adjustedDate;
                while (navOnCurrentDate == null && attempts <= maxAttempts) {
                    searchDate = LocalDateUtility.getAdjustedDate(searchDate.minusDays(1));
                    navOnCurrentDate = navsBySchemeAndDate.get(schemeCode).get(searchDate);
                    attempts++;

                    if (navOnCurrentDate == null && attempts <= maxAttempts) {
                        log.trace(
                                "NAV lookup attempt {} failed for scheme {} on date {} for CAS ID: {}",
                                attempts,
                                schemeCode,
                                searchDate,
                                userCASDetails.getId());
                    }
                }
                if (navOnCurrentDate != null) {
                    if (attempts > 0) {
                        log.debug(
                                "Found NAV for scheme {} on fallback date {} (original: {}) after {} attempts for CAS ID: {}",
                                schemeCode,
                                searchDate,
                                adjustedDate,
                                attempts,
                                userCASDetails.getId());
                    }

                    BigDecimal navValue = navOnCurrentDate.nav();
                    double units = cumulativeUnitsByScheme.get(schemeCode);
                    totalPortfolioValue = totalPortfolioValue.add(navValue.multiply(BigDecimal.valueOf(units)));
                } else {
                    navMissingCount.getAndIncrement();
                    log.warn(
                            "NAV not found for scheme {} on date {} after {} attempts for CAS ID: {}",
                            schemeCode,
                            adjustedDate,
                            maxAttempts,
                            userCASDetails.getId());
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
        });

        dailyProcessingStart.stop();
        log.debug(
                "Daily processing completed in {} ms for CAS ID: {}",
                dailyProcessingStart.getTotalTimeMillis(),
                userCASDetails.getId());

        // Step 11 : Bulk Insert Data
        log.info(
                "Saving {} portfolio value records for CAS ID: {}",
                portfolioValueEntityList.size(),
                userCASDetails.getId());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        userPortfolioValueRepository.saveAll(portfolioValueEntityList);

        stopWatch.stop();
        log.info(
                "Database save completed in {} ms for CAS ID: {}",
                stopWatch.getTotalTimeMillis(),
                userCASDetails.getId());

        methodStartTime.stop();
        log.info(
                "Portfolio value calculation completed in {} seconds for CAS ID: {}. "
                        + "Processed {} days, {} days had transactions, {} NAV lookup failures",
                methodStartTime.getTotalTimeSeconds(),
                userCASDetails.getId(),
        log.info(
                "Portfolio value calculation completed in {} seconds for CAS ID: {}. "
                        + "Processed {} days, {} days had transactions, {} NAV lookup failures",
                methodStartTime.getTotalTimeSeconds(),
                userCASDetails.getId(),
                portfolioValueEntityList.size(),
                daysWithTransactions.get(),
                navMissingCount.get());
    }
}
