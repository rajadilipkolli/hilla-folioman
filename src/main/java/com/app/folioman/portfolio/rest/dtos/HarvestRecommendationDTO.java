package com.app.folioman.portfolio.rest.dtos;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record HarvestRecommendationDTO(
        String schemeName,
        String folioNumber,
        BigDecimal unitsToRedeem,
        BigDecimal redemptionAmount,
        BigDecimal stcg,
        BigDecimal ltcg,
        BigDecimal estimatedTax,
        BigDecimal exitLoad,
        BigDecimal remainingUnits,
        double recommendationScore,
        @Nullable String reason) {}
