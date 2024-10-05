package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.portfolio.models.UserFolioDTO;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.models.response.PortfolioDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate).stream()
                        .map(portfolioDetails -> CompletableFuture.supplyAsync(
                                () -> createPortfolioDetailsDTO(portfolioDetails, asOfDate), taskExecutor))
                        .toList();
        return joinFutures(completableFutureList);
    }

    private PortfolioDetailsDTO createPortfolioDetailsDTO(
            PortfolioDetailsProjection portfolioDetails, LocalDate asOfDate) {
        MFSchemeDTO scheme;
        try {
            scheme = mfNavService.getNavByDateWithRetry(portfolioDetails.getSchemeId(), asOfDate);
            double totalValue = portfolioDetails.getBalanceUnits() * Double.parseDouble(scheme.nav());
            return new PortfolioDetailsDTO(
                    BigDecimal.valueOf(totalValue).setScale(4, RoundingMode.HALF_UP),
                    portfolioDetails.getSchemeName(),
                    portfolioDetails.getFolioNumber(),
                    scheme.date(),
                    0.0);
        } catch (NavNotFoundException navNotFoundException) {
            // Will happen in case of NFO where units are allocated but not ready for subscription
            LOGGER.error(
                    "NavNotFoundException occurred for scheme : {} on adjusted date :{}",
                    portfolioDetails.getSchemeId(),
                    asOfDate,
                    navNotFoundException);
            // Use a default NAV value or handle as needed
            double defaultNav = 10.0;
            double totalValue = portfolioDetails.getBalanceUnits() * defaultNav;
            return new PortfolioDetailsDTO(
                    BigDecimal.valueOf(totalValue),
                    portfolioDetails.getSchemeName(),
                    portfolioDetails.getFolioNumber(),
                    asOfDate.toString(),
                    0.0);
        }
    }
}
