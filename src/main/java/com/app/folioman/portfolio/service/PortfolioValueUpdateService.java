package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.portfolio.entities.FolioScheme;
import com.app.folioman.portfolio.entities.SchemeValue;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserPortfolioValue;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.FIFOUnits;
import com.app.folioman.portfolio.models.ProcessedTransaction;
import com.app.folioman.portfolio.models.request.TransactionType;
import com.app.folioman.portfolio.repository.FolioSchemeRepository;
import com.app.folioman.portfolio.repository.SchemeValueRepository;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
import com.app.folioman.portfolio.util.XirrCalculator;
import com.app.folioman.shared.LocalDateUtility;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

@Service
public class PortfolioValueUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioValueUpdateService.class);

    // Add a constant for excluded transaction types
    private static final EnumSet<TransactionType> TAX_TRANSACTION_TYPES = EnumSet.of(
            TransactionType.STAMP_DUTY_TAX, TransactionType.TDS_TAX, TransactionType.STT_TAX, TransactionType.MISC);

    // Add a constant for redemption transaction types
    private static final EnumSet<TransactionType> REDEMPTION_TRANSACTION_TYPES =
            EnumSet.of(TransactionType.REDEMPTION, TransactionType.SWITCH_OUT);

    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final MFNavService mfNavService;
    private final FolioSchemeRepository folioSchemeRepository;
    private final SchemeValueRepository schemeValueRepository;
    private final UserTransactionDetailsRepository userTransactionDetailsRepository;

    PortfolioValueUpdateService(
            UserPortfolioValueRepository userPortfolioValueRepository,
            MFNavService mfNavService,
            FolioSchemeRepository folioSchemeRepository,
            SchemeValueRepository schemeValueRepository,
            UserTransactionDetailsRepository userTransactionDetailsRepository) {
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.mfNavService = mfNavService;
        this.folioSchemeRepository = folioSchemeRepository;
        this.schemeValueRepository = schemeValueRepository;
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
    }

    @Async
    public void updatePortfolioValue(UserCASDetails userCASDetails) {
        handleDailyPortFolioValueUpdate(userCASDetails);
        userCASDetails.getFolios().forEach(userFolioDetails -> {
            Long portfolioId = userFolioDetails.getId();

            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            LocalDate fromDate1 = today;
            if (!CollectionUtils.isEmpty(userFolioDetails.getSchemes())) {
                fromDate1 = userFolioDetails.getSchemes().stream()
                        .map(userSchemeDetails ->
                                userSchemeDetails.getCreatedDate().toLocalDate())
                        .min(LocalDate::compareTo)
                        .orElse(today);
            }

            LocalDate fromDate2 = today;

            SchemeValue schemeValue =
                    schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(portfolioId);
            if (schemeValue != null) {
                fromDate2 = schemeValue.getDate();
            }

            LocalDate startDateMin = fromDate1.isBefore(fromDate2) ? fromDate1 : fromDate2;

            List<FolioScheme> schemes = folioSchemeRepository.findByUserFolioDetails_Id(portfolioId);

            LOGGER.info("Computing daily scheme values...");
            List<Map<String, Object>> schemeResults = new ArrayList<>();
            List<Long> schemeListFromDB = userFolioDetails.getSchemes().stream()
                    .map(UserSchemeDetails::getId)
                    .toList();
            for (FolioScheme folioScheme : schemes) {

                LocalDate schemeFromDate = schemeListFromDB.contains(
                                folioScheme.getUserSchemeDetails().getId())
                        //                        ? parseDate(schemeDates.get(folioScheme.getId()))
                        ? folioScheme.getUserSchemeDetails().getCreatedDate().toLocalDate()
                        : startDateMin;

                Optional<SchemeValue> schemeValueOpt =
                        schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(
                                folioScheme.getId(), schemeFromDate);
                List<UserTransactionDetails> oldTransactions =
                        userTransactionDetailsRepository.findByUserSchemeDetails_IdAndTransactionDateBefore(
                                folioScheme.getId(), schemeFromDate);
                List<UserTransactionDetails> newTransactions =
                        userTransactionDetailsRepository.findByUserSchemeDetails_IdAndTransactionDateGreaterThanEqual(
                                folioScheme.getId(), schemeFromDate);

                LocalDate fromDate = schemeValueOpt.map(SchemeValue::getDate).orElse(null);

                FIFOUnits fifo = new FIFOUnits();

                for (UserTransactionDetails txn : oldTransactions) {
                    fifo.addTransaction(txn);
                }

                List<ProcessedTransaction> transactionsProcessed = processNewTransactions(fifo, newTransactions);
                Map<String, Object> schemeData =
                        calculateSchemeData(fifo, schemeValueOpt, folioScheme.getId(), transactionsProcessed, today);
                if (schemeData != null) {
                    schemeResults.add(schemeData);
                }
            }

            // Further processing of FolioValue and PortfolioValue
            updateFolioAndPortfolioValues(schemeResults, startDateMin);
        });
    }

    private void updateFolioAndPortfolioValues(List<Map<String, Object>> schemeResults, LocalDate startDateMin) {
        if (schemeResults.isEmpty()) {
            LOGGER.info("No scheme data to process. Exiting...");
            return;
        }

        LOGGER.info("Processing {} schemes for SchemeValue generation", schemeResults.size());

        // Step 1: Generate and save SchemeValue entities for all schemes
        List<SchemeValue> allSchemeValues = new ArrayList<>();
        Map<Long, FolioScheme> folioSchemeUpdates = new HashMap<>();

        for (Map<String, Object> schemeData : schemeResults) {
            Long schemeId = (Long) schemeData.get("schemeId");
            Long amfiCode = (Long) schemeData.get("amfiCode");
            LocalDate fromDate = (LocalDate) schemeData.get("fromDate");
            LocalDate toDate = (LocalDate) schemeData.get("toDate");

            @SuppressWarnings("unchecked")
            List<ProcessedTransaction> processedTransactions =
                    (List<ProcessedTransaction>) schemeData.get("processedTransactions");

            @SuppressWarnings("unchecked")
            Map<LocalDate, BigDecimal> navsByDate = (Map<LocalDate, BigDecimal>) schemeData.get("navsByDate");

            UserSchemeDetails userSchemeDetails = (UserSchemeDetails) schemeData.get("userSchemeDetails");

            if (processedTransactions == null || processedTransactions.isEmpty()) {
                LOGGER.warn("No processed transactions for scheme {}, skipping", schemeId);
                continue;
            }

            // Generate SchemeValue records for this scheme
            List<SchemeValue> schemeValues =
                    generateSchemeValues(processedTransactions, navsByDate, userSchemeDetails, fromDate, toDate);
            allSchemeValues.addAll(schemeValues);

            // Prepare FolioScheme update with latest valuation
            if (!schemeValues.isEmpty()) {
                SchemeValue latestSchemeValue = schemeValues.getLast();
                FolioScheme folioScheme = findOrCreateFolioScheme(schemeId, userSchemeDetails);
                folioScheme.setValuation(latestSchemeValue.getValue());
                folioScheme.setValuationDate(latestSchemeValue.getDate());
                folioSchemeUpdates.put(schemeId, folioScheme);
            }
        }

        // Step 2: Bulk save SchemeValue entities
        if (!allSchemeValues.isEmpty()) {
            LOGGER.info("Saving {} SchemeValue records", allSchemeValues.size());
            schemeValueRepository.saveAll(allSchemeValues);
            LOGGER.info("SchemeValue data imported successfully");
        }

        // Step 3: Update FolioScheme entities
        if (!folioSchemeUpdates.isEmpty()) {
            LOGGER.info("Updating {} FolioScheme records", folioSchemeUpdates.size());
            folioSchemeRepository.saveAll(folioSchemeUpdates.values());
            LOGGER.info("FolioScheme updated successfully");
        }

        // Note: UserPortfolioValue is already handled in the main handleDailyPortFolioValueUpdate flow
        LOGGER.info("Portfolio value update completed");
    }

    /**
     * Generates SchemeValue records for each day in the date range based on processed transactions and NAV data.
     */
    private List<SchemeValue> generateSchemeValues(
            List<ProcessedTransaction> processedTransactions,
            Map<LocalDate, BigDecimal> navsByDate,
            UserSchemeDetails userSchemeDetails,
            LocalDate fromDate,
            LocalDate toDate) {

        List<SchemeValue> schemeValues = new ArrayList<>();

        // Create a map of transactions by date for quick lookup
        Map<LocalDate, ProcessedTransaction> transactionsByDate = processedTransactions.stream()
                .collect(Collectors.toMap(
                        ProcessedTransaction::date,
                        pt -> pt,
                        (existing, replacement) -> replacement // Keep last transaction if multiple on same date
                        ));

        // Track the latest known state
        BigDecimal currentInvested = BigDecimal.ZERO;
        BigDecimal currentAverage = BigDecimal.ZERO;
        BigDecimal currentBalance = BigDecimal.ZERO;

        // Generate SchemeValue for each day
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            // Update state if there's a transaction on this date
            if (transactionsByDate.containsKey(date)) {
                ProcessedTransaction pt = transactionsByDate.get(date);
                currentInvested = pt.invested();
                currentAverage = pt.average();
                currentBalance = pt.balance();
            }

            // Skip if no activity yet
            if (currentBalance.compareTo(BigDecimal.ZERO) == 0 && currentInvested.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // Get NAV for this date (with fallback to previous available NAV)
            BigDecimal nav = findNavForDate(date, navsByDate);

            if (nav == null) {
                LOGGER.warn(
                        "NAV not found for scheme {} on date {}, skipping this date", userSchemeDetails.getId(), date);
                continue;
            }

            // Calculate current value: balance * NAV
            BigDecimal currentValue = currentBalance.multiply(nav).setScale(2, RoundingMode.HALF_UP);

            // Create SchemeValue entity
            SchemeValue schemeValue = new SchemeValue();
            schemeValue.setDate(date);
            schemeValue.setInvested(currentInvested);
            schemeValue.setValue(currentValue);
            schemeValue.setAvgNav(currentAverage);
            schemeValue.setNav(nav);
            schemeValue.setBalance(currentBalance);
            schemeValue.setUserSchemeDetails(userSchemeDetails);

            schemeValues.add(schemeValue);
        }

        return schemeValues;
    }

    /**
     * Finds NAV for a specific date, with fallback to the most recent previous NAV if not available.
     */
    private BigDecimal findNavForDate(LocalDate date, Map<LocalDate, BigDecimal> navsByDate) {
        if (navsByDate.containsKey(date)) {
            return navsByDate.get(date);
        }

        // Fallback: look for most recent NAV before this date
        LocalDate checkDate = date.minusDays(1);
        int attempts = 0;
        int maxAttempts = 10; // Look back up to 10 days

        while (attempts < maxAttempts) {
            if (navsByDate.containsKey(checkDate)) {
                return navsByDate.get(checkDate);
            }
            checkDate = checkDate.minusDays(1);
            attempts++;
        }

        return null; // No NAV found within lookback period
    }

    private Map<String, Object> calculateSchemeData(
            FIFOUnits fifo,
            Optional<SchemeValue> schemeValueOpt,
            Long schemeId,
            List<ProcessedTransaction> transactionsProcessed,
            LocalDate today) {

        if (fifo.getBalance().compareTo(BigDecimal.valueOf(1e-3)) <= 0 && schemeValueOpt.isEmpty()) {
            LOGGER.info("Skipping scheme {} - no balance and no previous values", schemeId);
            return null;
        }

        if (transactionsProcessed.isEmpty()) {
            LOGGER.info("Skipping scheme {} - no processed transactions", schemeId);
            return null;
        }

        LocalDate fromDate = transactionsProcessed.getFirst().date();
        LocalDate toDate = calculateToDate(fifo, transactionsProcessed, schemeId, today);

        if (toDate == null) {
            return null;
        }

        // Fetch NAV data for the date range
        UserSchemeDetails userSchemeDetails =
                schemeValueOpt.map(SchemeValue::getUserSchemeDetails).orElse(null);

        if (userSchemeDetails == null) {
            LOGGER.warn("Cannot find UserSchemeDetails for scheme {}", schemeId);
            return null;
        }

        Long amfiCode = userSchemeDetails.getAmfi();
        if (amfiCode == null) {
            LOGGER.warn("AMFI code not found for scheme {}, cannot fetch NAVs", schemeId);
            return null;
        }

        // Fetch NAVs for this scheme and date range
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navData =
                mfNavService.getNavsForSchemesAndDates(Set.of(amfiCode), fromDate, toDate);

        Map<LocalDate, BigDecimal> navsByDate = new HashMap<>();
        if (navData.containsKey(amfiCode)) {
            navData.get(amfiCode).forEach((date, projection) -> navsByDate.put(date, projection.nav()));
        }

        if (navsByDate.isEmpty()) {
            LOGGER.warn(
                    "No NAV data found for scheme {} (AMFI: {}) between {} and {}",
                    schemeId,
                    amfiCode,
                    fromDate,
                    toDate);
            return null;
        }

        // Return comprehensive data for further processing
        Map<String, Object> result = new HashMap<>();
        result.put("schemeId", schemeId);
        result.put("amfiCode", amfiCode);
        result.put("fromDate", fromDate);
        result.put("toDate", toDate);
        result.put("processedTransactions", transactionsProcessed);
        result.put("navsByDate", navsByDate);
        result.put("userSchemeDetails", userSchemeDetails);

        return result;
    }

    private LocalDate calculateToDate(
            FIFOUnits fifo, List<ProcessedTransaction> transactionsProcessed, Long schemeId, LocalDate today) {
        if (fifo.getBalance().compareTo(BigDecimal.valueOf(1e-3)) > 0) {
            return mfNavService
                    .findTopBySchemeIdOrderByDateDesc(schemeId)
                    .map(mfSchemeDTO -> LocalDate.parse(mfSchemeDTO.date()))
                    .orElse(today.minusDays(1));
        } else if (!transactionsProcessed.isEmpty()) {
            return transactionsProcessed.getLast().date();
        } else {
            LOGGER.info("Skipping scheme :: {}", schemeId);
            return null;
        }
    }

    private List<ProcessedTransaction> processNewTransactions(
            FIFOUnits fifo, List<UserTransactionDetails> newTransactions) {
        List<ProcessedTransaction> processedTransactions = new ArrayList<>();
        for (UserTransactionDetails txn : newTransactions) {
            fifo.addTransaction(txn);
            processedTransactions.add(new ProcessedTransaction(
                    txn.getTransactionDate(), fifo.getInvested(), fifo.getAverage(), fifo.getBalance()));
        }
        return processedTransactions;
    }

    private void handleDailyPortFolioValueUpdate(UserCASDetails userCASDetails) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info(
                "Starting portfolio value update for CAS ID: {}, PAN: {}",
                userCASDetails.getId(),
                userCASDetails.getFolios().getFirst().getPan());

        try {
            List<UserTransactionDetails> transactionList = collectRelevantTransactions(userCASDetails);

            calculateAndInsertDailyPortfolioValues(transactionList, userCASDetails);

            stopWatch.stop();
            LOGGER.info(
                    "Completed portfolio value update for CAS ID: {}, took {} seconds",
                    userCASDetails.getId(),
                    stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            LOGGER.error("Error updating portfolio values for CAS ID: {}", userCASDetails.getId(), e);
        }
    }

    private List<UserTransactionDetails> collectRelevantTransactions(UserCASDetails userCASDetails) {
        LOGGER.debug("Collecting and filtering transactions for CAS ID: {}", userCASDetails.getId());
        List<UserTransactionDetails> transactionList = userCASDetails.getFolios().stream()
                .flatMap(folio -> folio.getSchemes().stream())
                .flatMap(scheme -> scheme.getTransactions().stream())
                .filter(transaction -> !TAX_TRANSACTION_TYPES.contains(transaction.getType()))
                .sorted(Comparator.comparing(UserTransactionDetails::getTransactionDate))
                .toList();

        LOGGER.info("Found {} relevant transactions for CAS ID: {}", transactionList.size(), userCASDetails.getId());
        return transactionList;
    }

    private void calculateAndInsertDailyPortfolioValues(
            List<UserTransactionDetails> transactionList, UserCASDetails userCASDetails) {

        StopWatch methodStartTime = new StopWatch();
        methodStartTime.start();

        // Handle empty transaction list
        if (transactionList == null || transactionList.isEmpty()) {
            LOGGER.info(
                    "No transactions found for CAS ID: {}. Skipping portfolio value calculation.",
                    userCASDetails.getId());
            return;
        }

        // Prepare data and date range
        LocalDate startDate = transactionList.getFirst().getTransactionDate();
        LocalDate endDate = LocalDateUtility.getYesterday();

        LOGGER.debug(
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
        LOGGER.debug(
                "Portfolio values calculated and inserted, took {} seconds", methodStartTime.getTotalTimeSeconds());
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

        LOGGER.debug("Fetching NAVs for {} schemes for CAS ID: {}", schemeCodes.size(), userCASDetails.getId());
        StopWatch navFetchStart = new StopWatch();
        navFetchStart.start();

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navsBySchemeAndDate =
                mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

        navFetchStart.stop();
        LOGGER.debug(
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
            LOGGER.warn(
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
                LOGGER.warn(
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

            LOGGER.debug("Saved XIRR {} for scheme ID {}", xirrValue, schemeDetailId);
        } catch (Exception e) {
            LOGGER.warn("Unable to calculate XIRR for scheme ID {}: {}", schemeDetailId, e.getMessage());
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
                    LOGGER.debug("Overall portfolio XIRR calculated: {}", overallXirr);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to calculate overall portfolio XIRR: {}", e.getMessage());
        }
    }

    /**
     * Find an existing FolioScheme entity or create a new one if it doesn't exist.
     */
    private FolioScheme findOrCreateFolioScheme(Long schemeDetailId, UserSchemeDetails userSchemeDetails) {
        String operationId = generateOperationId(schemeDetailId);
        LOGGER.debug("[{}] Looking up FolioScheme for schemeDetailId: {}", operationId, schemeDetailId);

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
                LOGGER.debug("[{}] Found existing FolioScheme for schemeDetailId: {}", operationId, schemeDetailId);
            }
            return folioScheme;
        } catch (Exception e) {
            LOGGER.error("[{}] Error while finding FolioScheme for schemeDetailId: {}", operationId, schemeDetailId, e);
            throw e;
        }
    }

    private FolioScheme createNewFolioScheme(
            Long schemeDetailId, UserSchemeDetails userSchemeDetails, String operationId) {
        LOGGER.debug(
                "[{}] No existing FolioScheme found, creating new one for schemeDetailId: {}",
                operationId,
                schemeDetailId);
        FolioScheme folioScheme = new FolioScheme();
        folioScheme.setUserSchemeDetails(userSchemeDetails);
        folioScheme.setUserFolioDetails(userSchemeDetails.getUserFolioDetails());
        LOGGER.debug("[{}] New FolioScheme created for schemeDetailId: {}", operationId, schemeDetailId);
        return folioScheme;
    }
}
