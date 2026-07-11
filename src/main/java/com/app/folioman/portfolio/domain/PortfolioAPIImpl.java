package com.app.folioman.portfolio.domain;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.PortfolioSummaryProjection;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingRequest;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingResponse;
import com.app.folioman.portfolio.domain.models.projection.PortfolioValueDateProjection;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingResponseDTO;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.HarvestRecommendationDTO;
import com.app.folioman.portfolio.rest.dtos.HarvestSummaryDTO;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import com.app.folioman.shared.LocalDateUtility;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortfolioAPIImpl implements PortfolioAPI {

    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserDetailService userDetailService;
    private final PdfProcessingService pdfProcessingService;
    private final UserCASDetailsRepository userCASDetailsRepository;
    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final CapitalGainsHarvestingService capitalGainsHarvestingService;
    private final PortfolioSummaryService portfolioSummaryService;

    PortfolioAPIImpl(
            UserTransactionDetailsService userTransactionDetailsService,
            UserDetailService userDetailService,
            PdfProcessingService pdfProcessingService,
            UserCASDetailsRepository userCASDetailsRepository,
            UserPortfolioValueRepository userPortfolioValueRepository,
            CapitalGainsHarvestingService capitalGainsHarvestingService,
            PortfolioSummaryService portfolioSummaryService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userDetailService = userDetailService;
        this.pdfProcessingService = pdfProcessingService;
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.capitalGainsHarvestingService = capitalGainsHarvestingService;
        this.portfolioSummaryService = portfolioSummaryService;
    }

    public Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan) {
        return userTransactionDetailsService.getInvestmentReturnsByPan(pan);
    }

    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
    }

    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerYear(pan);
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        return userDetailService.upload(multipartFile);
    }

    public UploadFileResponse uploadFromDto(CasDTO casDTO) {
        return userDetailService.uploadFromDto(casDTO);
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        return userDetailService.getPortfolioByPAN(panNumber, asOfDate);
    }

    public CasDTO convertPdfCasToJson(MultipartFile pdfFile, String password) throws IOException {
        return pdfProcessingService.convertPdfCasToJson(pdfFile, password);
    }

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
                .map(cas -> {
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
    public Optional<com.app.folioman.portfolio.rest.dtos.PortfolioSummaryDTO> getPortfolioSummary(
            Long casId, String userEmail) {
        return portfolioSummaryService.getPortfolioSummary(casId, userEmail);
    }

    public CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(
            String pan, CapitalGainsHarvestingRequestDTO request) {
        CapitalGainsHarvestingRequest domainRequest = new CapitalGainsHarvestingRequest(
                pan,
                request.asOfDate(),
                request.financialYear(),
                request.targetAmount(),
                request.taxRegime(),
                request.includeStcg(),
                request.includeLtcg(),
                request.includeExitLoad(),
                request.existingRealizedGains(),
                request.exemptionOverride(),
                request.minRedemptionAmount(),
                request.schemeFilters(),
                request.amcFilters());

        CapitalGainsHarvestingResponse response = capitalGainsHarvestingService.generateHarvestingPlan(domainRequest);

        List<HarvestRecommendationDTO> recommendationDTOs = response.recommendations().stream()
                .map(r -> new HarvestRecommendationDTO(
                        r.schemeName(),
                        r.folioNumber(),
                        r.unitsToRedeem(),
                        r.redemptionAmount(),
                        r.stcg(),
                        r.ltcg(),
                        r.estimatedTax(),
                        r.exitLoad(),
                        r.remainingUnits(),
                        r.recommendationScore(),
                        r.reason()))
                .toList();

        HarvestSummaryDTO summaryDTO = new HarvestSummaryDTO(
                response.summary().totalStcg(),
                response.summary().totalLtcg(),
                response.summary().totalEstimatedTax(),
                response.summary().totalExitLoad(),
                response.summary().totalRedemptionValue(),
                response.summary().remainingPortfolioValue());

        return new CapitalGainsHarvestingResponseDTO(recommendationDTOs, summaryDTO);
    }
}
