package com.app.folioman.portfolio.domain;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.portfolio.domain.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.rest.dtos.PortfolioDetailsDTO;
import com.app.folioman.portfolio.rest.dtos.UserFolioDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
class PortfolioServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioServiceHelper.class);
    private final JsonMapper mapper;
    private final UserCASDetailsService userCASDetailsService;
    private final MFNavService mfNavService;
    private final Executor virtualThreadExecutor;

    PortfolioServiceHelper(
            JsonMapper mapper,
            UserCASDetailsService userCASDetailsService,
            MFNavService mfNavService,
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.mapper = mapper;
        this.userCASDetailsService = userCASDetailsService;
        this.mfNavService = mfNavService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    <T> T readValue(byte[] bytes, Class<T> responseClassType) {
        return this.mapper.readValue(bytes, responseClassType);
    }

    long countTransactionsByUserFolioDTOList(List<UserFolioDTO> folios) {
        return folios.stream()
                .flatMap(folio -> folio.schemes().stream())
                .mapToLong(scheme -> scheme.transactions().size())
                .sum();
    }

    <T> List<T> joinFutures(List<CompletableFuture<T>> futures) {
        return futures.stream().map(CompletableFuture::join).toList();
    }

    List<PortfolioDetailsDTO> getPortfolioDetailsByPANAndAsOfDate(String panNumber, LocalDate asOfDate) {
        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate).stream()
                        .map(portfolioDetails -> CompletableFuture.supplyAsync(
                                () -> createPortfolioDetailsDTO(portfolioDetails, asOfDate), virtualThreadExecutor))
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
