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
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PortfolioValueUpdateService {

    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final MFNavService mfNavService;

    public PortfolioValueUpdateService(
            UserPortfolioValueRepository userPortfolioValueRepository, MFNavService mfNavService) {
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.mfNavService = mfNavService;
    }

    @Async
    public void updatePortfolioValue(UserCASDetails userCASDetails) {
        // Step 1: Collect all transactions from the folios and schemes
        List<UserTransactionDetails> transactionList = userCASDetails.getFolios().stream()
                .flatMap(folio -> folio.getSchemes().stream())
                .flatMap(scheme -> scheme.getTransactions().stream())
                .filter(transaction -> !EnumSet.of(
                                TransactionType.STAMP_DUTY_TAX, TransactionType.TDS_TAX, TransactionType.STT_TAX)
                        .contains(transaction.getType()))
                .sorted(Comparator.comparing(UserTransactionDetails::getTransactionDate))
                .toList();

        calculateAndInsertDailyPortfolioValues(transactionList, userCASDetails);
    }

    private void calculateAndInsertDailyPortfolioValues(
            List<UserTransactionDetails> transactionList, UserCASDetails userCASDetails) {

        // Step 2: Prepare data structures
        LocalDate startDate = transactionList.getFirst().getTransactionDate();
        LocalDate endDate = LocalDateUtility.getYesterday();

        // Step 3: Group transactions by date for efficient lookup
        Map<LocalDate, List<UserTransactionDetails>> transactionsByDate =
                transactionList.stream().collect(Collectors.groupingBy(UserTransactionDetails::getTransactionDate));

        // Step 4: Pre-fetch NAVs in bulk for all schemes and dates in one go
        Set<Long> schemeCodes = transactionList.stream()
                .map(transaction -> transaction.getUserSchemeDetails().getAmfi())
                .collect(Collectors.toSet());
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate =
                mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

        // Step 5: Pre-compute the total cumulative invested amount and units for each scheme
        Map<Long, BigDecimal> cumulativeInvestedAmountByScheme = new HashMap<>();
        Map<Long, Double> cumulativeUnitsByScheme = new HashMap<>();

        // Step 6: Process each day in the date range
        List<UserPortfolioValue> portfolioValueEntityList = new ArrayList<>();
        startDate.datesUntil(endDate.plusDays(1)).forEach(currentDate -> {
            BigDecimal totalPortfolioValue = BigDecimal.ZERO;

            // Step 7: Update invested amount and units if transactions are present on this date
            List<UserTransactionDetails> dailyTransactions =
                    transactionsByDate.getOrDefault(currentDate, Collections.emptyList());
            dailyTransactions.forEach(transaction -> {
                Long amfiCode = transaction.getUserSchemeDetails().getAmfi();
                BigDecimal transactionAmount = transaction.getAmount();
                double transactionUnits = transaction.getUnits();

                // Update cumulative invested amount and units for the scheme
                cumulativeInvestedAmountByScheme.merge(amfiCode, transactionAmount, BigDecimal::add);
                cumulativeUnitsByScheme.merge(amfiCode, transactionUnits, Double::sum);
            });

            // Step 8: Calculate the portfolio value by fetching NAVs in bulk for the day
            for (Long schemeCode : cumulativeUnitsByScheme.keySet()) {
                MFSchemeNavProjection navOnCurrentDate =
                        navsBySchemeAndDate.get(schemeCode).get(currentDate);
                if (navOnCurrentDate != null) {
                    BigDecimal navValue = navOnCurrentDate.nav();
                    double units = cumulativeUnitsByScheme.get(schemeCode);
                    totalPortfolioValue = totalPortfolioValue.add(navValue.multiply(BigDecimal.valueOf(units)));
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
        // Step 11 : Bulk Insert Data
        userPortfolioValueRepository.saveAll(portfolioValueEntityList);
    }
}
