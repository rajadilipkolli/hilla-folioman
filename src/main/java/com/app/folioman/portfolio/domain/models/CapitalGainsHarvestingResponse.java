package com.app.folioman.portfolio.domain.models;

import java.util.List;

public record CapitalGainsHarvestingResponse(List<HarvestRecommendation> recommendations, HarvestSummary summary) {}
