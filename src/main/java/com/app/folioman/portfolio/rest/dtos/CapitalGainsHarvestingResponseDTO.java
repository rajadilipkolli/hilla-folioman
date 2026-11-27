package com.app.folioman.portfolio.rest.dtos;

import java.util.List;

public record CapitalGainsHarvestingResponseDTO(
        List<HarvestRecommendationDTO> recommendations, HarvestSummaryDTO summary) {}
