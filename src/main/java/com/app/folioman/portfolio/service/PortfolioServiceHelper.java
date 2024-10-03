package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.portfolio.models.UserFolioDTO;
import com.app.folioman.portfolio.models.response.PortfolioDetailsDTO;
import com.app.folioman.shared.MFNavService;
import com.app.folioman.shared.MFSchemeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
class PortfolioServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioServiceHelper.class);
    private final ObjectMapper mapper;
    private final UserCASDetailsService userCASDetailsService;
    private final MFNavService mfNavService;
    private final TaskExecutor taskExecutor;

    PortfolioServiceHelper(
            ObjectMapper mapper,
            UserCASDetailsService userCASDetailsService,
            MFNavService mfNavService,
            @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.mapper = mapper;
        this.userCASDetailsService = userCASDetailsService;
        this.mfNavService = mfNavService;
        this.taskExecutor = taskExecutor;
    }

    public <T> T readValue(byte[] bytes, Class<T> responseClassType) throws IOException {
        return this.mapper.readValue(bytes, responseClassType);
    }

    public long countTransactionsByUserFolioDTOList(List<UserFolioDTO> folios) {
        return folios.stream()
                .flatMap(folio -> folio.schemes().stream())
                .flatMap(scheme -> scheme.transactions().stream())
                .count();
    }

    public <T> List<T> joinFutures(List<CompletableFuture<T>> futures) {
        return futures.stream().map(CompletableFuture::join).toList();
    }

    public List<PortfolioDetailsDTO> getPortfolioDetailsByPANAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return joinFutures(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate).stream()
                .map(portfolioDetails -> CompletableFuture.supplyAsync(
                        () -> {
                            MFSchemeDTO scheme;
                            try {
                                scheme = mfNavService.getNavByDateWithRetry(portfolioDetails.getSchemeId(), asOfDate);
                            } catch (NavNotFoundException navNotFoundException) {
                                // Will happen in case of NFO where units are allocated but not ready for subscription
                                LOGGER.error(
                                        "NavNotFoundException occurred for scheme : {} on adjusted date :{}",
                                        portfolioDetails.getSchemeId(),
                                        asOfDate,
                                        navNotFoundException);
                                scheme = new MFSchemeDTO(null, null, null, null, "10", asOfDate.toString(), null);
                            }
                            double totalValue = portfolioDetails.getBalanceUnits() * Double.parseDouble(scheme.nav());
                            return new PortfolioDetailsDTO(
                                    Math.round(totalValue * 100.0) / 100.0,
                                    portfolioDetails.getSchemeName(),
                                    portfolioDetails.getFolioNumber(),
                                    scheme.date(),
                                    0.0);
                        },
                        taskExecutor))
                .toList());
    }
}
