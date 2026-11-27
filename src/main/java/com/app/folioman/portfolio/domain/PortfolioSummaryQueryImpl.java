package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.PortfolioSummaryProjection;
import com.app.folioman.portfolio.PortfolioSummaryQuery;
import com.app.folioman.portfolio.domain.models.projection.PortfolioValueDateProjection;
import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioSummaryDTO;
import com.app.folioman.shared.CacheNames;
import com.app.folioman.shared.LocalDateUtility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class PortfolioSummaryQueryImpl implements PortfolioSummaryQuery {

    private final UserCASDetailsRepository userCASDetailsRepository;
    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final PortfolioSummaryService portfolioSummaryService;

    PortfolioSummaryQueryImpl(
            UserCASDetailsRepository userCASDetailsRepository,
            UserPortfolioValueRepository userPortfolioValueRepository,
            PortfolioSummaryService portfolioSummaryService) {
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.portfolioSummaryService = portfolioSummaryService;
    }

    @Override
    public List<PortfolioSummaryProjection> getPortfolioSummariesByEmail(String email) {
        List<UserCasDetailsEntity> casList = userCASDetailsRepository.findAllByInvestorInfoEntityEmail(email);
        return casList.stream()
                .<PortfolioSummaryProjection>map(cas -> {
                    var projectionOpt = userPortfolioValueRepository.getLatestPortfolioValueByCasId(cas.getId());

                    BigDecimal latestValue = projectionOpt
                            .map(PortfolioValueDateProjection::getValue)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal latestXirr = projectionOpt
                            .map(PortfolioValueDateProjection::getXirr)
                            .orElse(null);

                    return new PortfolioSummaryProjection() {
                        @Override
                        public String getName() {
                            return cas.getInvestorInfoEntity() != null
                                    ? cas.getInvestorInfoEntity().getName()
                                    : "Unknown";
                        }

                        @Override
                        public BigDecimal getValue() {
                            return latestValue;
                        }

                        @Override
                        public @Nullable BigDecimal getXirr() {
                            return latestXirr;
                        }
                    };
                })
                .toList();
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.PORTFOLIO_HISTORY_CACHE,
            key = "'history_' + #casId + '_' + #userEmail + '_' + #from + '_' + #to")
    public Optional<PortfolioHistoryDTO> getPortfolioHistory(
            Long casId, String userEmail, LocalDate from, LocalDate to) {
        return userCASDetailsRepository
                .findById(casId)
                .filter(cas -> cas.getInvestorInfoEntity() != null)
                .filter(cas -> userEmail.equals(cas.getInvestorInfoEntity().getEmail()))
                .map(_ -> {
                    List<UserPortfolioValueEntity> portfolioValues =
                            userPortfolioValueRepository.findByUserCasDetailsEntity_IdAndDateBetween(casId, from, to);

                    List<UserPortfolioValueEntity> sortedValues = portfolioValues.stream()
                            .sorted(Comparator.comparing(UserPortfolioValueEntity::getDate))
                            .toList();

                    List<Number[]> invested = sortedValues.stream()
                            .map(entry ->
                                    new Number[] {LocalDateUtility.toEpochMillis(entry.getDate()), entry.getInvested()})
                            .toList();

                    List<Number[]> value = sortedValues.stream()
                            .map(entry ->
                                    new Number[] {LocalDateUtility.toEpochMillis(entry.getDate()), entry.getValue()})
                            .toList();

                    return new PortfolioHistoryDTO(invested, value);
                });
    }

    @Override
    @Cacheable(cacheNames = CacheNames.SUMMARY_CACHE, key = "'summary_' + #casId + '_' + #userEmail")
    public Optional<PortfolioSummaryDTO> getPortfolioSummary(Long casId, String userEmail) {
        return portfolioSummaryService.getPortfolioSummary(casId, userEmail);
    }
}
