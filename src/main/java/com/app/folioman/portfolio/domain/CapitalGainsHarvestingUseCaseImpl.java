package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.CapitalGainsHarvestingUseCase;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingRequest;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingResponse;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingResponseDTO;
import com.app.folioman.portfolio.rest.dtos.HarvestRecommendationDTO;
import com.app.folioman.portfolio.rest.dtos.HarvestSummaryDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class CapitalGainsHarvestingUseCaseImpl implements CapitalGainsHarvestingUseCase {

    private final CapitalGainsHarvestingService capitalGainsHarvestingService;

    CapitalGainsHarvestingUseCaseImpl(CapitalGainsHarvestingService capitalGainsHarvestingService) {
        this.capitalGainsHarvestingService = capitalGainsHarvestingService;
    }

    @Override
    public CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(
            String pan, String email, CapitalGainsHarvestingRequestDTO request) {
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

        CapitalGainsHarvestingResponse response =
                capitalGainsHarvestingService.generateHarvestingPlan(domainRequest, email);

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
