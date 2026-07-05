package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.PortfolioSummaryProjection;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingRequest;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingResponse;
import com.app.folioman.portfolio.domain.models.projection.PortfolioValueDateProjection;
import com.app.folioman.portfolio.rest.dtos.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
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

    PortfolioAPIImpl(
            UserTransactionDetailsService userTransactionDetailsService,
            UserDetailService userDetailService,
            PdfProcessingService pdfProcessingService,
            UserCASDetailsRepository userCASDetailsRepository,
            UserPortfolioValueRepository userPortfolioValueRepository,
            CapitalGainsHarvestingService capitalGainsHarvestingService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userDetailService = userDetailService;
        this.pdfProcessingService = pdfProcessingService;
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.capitalGainsHarvestingService = capitalGainsHarvestingService;
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

    public CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(
            String pan, CapitalGainsHarvestingRequestDTO request) {
        CapitalGainsHarvestingRequest domainRequest = new CapitalGainsHarvestingRequest(
                pan,
                request.targetAmount(),
                request.includeStcg(),
                request.includeLtcg(),
                request.includeExitLoad(),
                request.exemptionOverride(),
                request.targetAmount(), // Not using minRedemptionAmount from DTO, map it or just pass null if
                // not needed
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
