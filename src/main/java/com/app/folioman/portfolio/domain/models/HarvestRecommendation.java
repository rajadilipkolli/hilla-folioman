package com.app.folioman.portfolio.domain.models;

import java.math.BigDecimal;
import java.util.List;

public record HarvestRecommendation(
        String schemeName,
        String folioNumber,
        BigDecimal unitsToRedeem,
        BigDecimal redemptionAmount,
        List<HarvestLot> consumedLots,
        BigDecimal stcg,
        BigDecimal ltcg,
        BigDecimal estimatedTax,
        BigDecimal exitLoad,
        BigDecimal remainingUnits,
        double recommendationScore,
        String reason) {}
