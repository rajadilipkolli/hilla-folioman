package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.FolioScheme;
import com.app.folioman.portfolio.entities.SchemeValue;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.repository.FolioSchemeRepository;
import com.app.folioman.portfolio.repository.SchemeValueRepository;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
import com.app.folioman.shared.MFNavService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PortfolioValueUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioValueUpdateService.class);

    private final SchemeValueRepository schemeValueRepository;
    private final FolioSchemeRepository folioSchemeRepository;
    private final UserTransactionDetailsRepository userTransactionDetailsRepository;
    private final MFNavService mfNavService;

    public PortfolioValueUpdateService(
            SchemeValueRepository schemeValueRepository,
            FolioSchemeRepository folioSchemeRepository,
            UserTransactionDetailsRepository userTransactionDetailsRepository,
            MFNavService mfNavService) {
        this.schemeValueRepository = schemeValueRepository;
        this.folioSchemeRepository = folioSchemeRepository;
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
        this.mfNavService = mfNavService;
    }

    @Async
    public void updatePortfolioValue(UserCASDetails userCASDetails) {
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

            log.info("Computing daily scheme values...");
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

                List<Map<String, Object>> transactionsProcessed = processNewTransactions(fifo, newTransactions);
                Map<String, Object> schemeData =
                        calculateSchemeData(fifo, schemeValueOpt, folioScheme.getId(), transactionsProcessed, today);
                schemeResults.add(schemeData);
            }

            // Further processing of FolioValue and PortfolioValue
            updateFolioAndPortfolioValues(schemeResults, startDateMin);
        });
    }

    private void updateFolioAndPortfolioValues(List<Map<String, Object>> schemeResults, LocalDate startDateMin) {
        if (schemeResults.isEmpty()) {
            log.info("No data found. Exiting...");
            return;
        }

        log.info("Importing SchemeValue data...");
        // Implement logic for bulk insertion of SchemeValue data

        log.info("Updating FolioValue...");
        // Update folio values based on scheme value updates

        log.info("Updating PortfolioValue...");
        // Update portfolio values based on updated folio values
    }

    private Map<String, Object> calculateSchemeData(
            FIFOUnits fifo,
            Optional<SchemeValue> schemeValueOpt,
            Long schemeId,
            List<Map<String, Object>> transactionsProcessed,
            LocalDate today) {
        if (fifo.getBalance().compareTo(BigDecimal.valueOf(1e-3)) <= 0 && schemeValueOpt.isEmpty()) {
            log.info("Skipping scheme :: {}", schemeId);
            return null;
        }

        LocalDate toDate = calculateToDate(fifo, transactionsProcessed, schemeId, today);
        // Calculate NAV and scheme value based on the transactions and historical NAVs
        // Implement logic similar to the Python version, creating data frames or collections for further processing

        return Map.of(
                "schemeId",
                schemeId,
                "fromDate",
                transactionsProcessed.getFirst().get("date"),
                "toDate",
                toDate);
    }

    private LocalDate calculateToDate(
            FIFOUnits fifo, List<Map<String, Object>> transactionsProcessed, Long schemeId, LocalDate today) {
        if (fifo.getBalance().compareTo(BigDecimal.valueOf(1e-3)) > 0) {
            return mfNavService
                    .findTopBySchemeIdOrderByDateDesc(schemeId)
                    .map(mfSchemeDTO -> LocalDate.parse(mfSchemeDTO.date()))
                    .orElse(today.minusDays(1));
        } else if (!transactionsProcessed.isEmpty()) {
            return (LocalDate) transactionsProcessed.getLast().get("date");
        } else {
            log.info("Skipping scheme :: {}", schemeId);
            return null;
        }
    }

    private List<Map<String, Object>> processNewTransactions(
            FIFOUnits fifo, List<UserTransactionDetails> newTransactions) {
        List<Map<String, Object>> processedTransactions = new ArrayList<>();
        for (UserTransactionDetails txn : newTransactions) {
            fifo.addTransaction(txn);
            Map<String, Object> txnData = new HashMap<>();
            txnData.put("date", txn.getTransactionDate());
            txnData.put("invested", fifo.getInvested());
            txnData.put("average", fifo.getAverage());
            txnData.put("balance", fifo.getBalance());
            processedTransactions.add(txnData);
        }
        return processedTransactions;
    }
}
