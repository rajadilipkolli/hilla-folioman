package com.app.folioman.portfolio.domain.models;

import java.math.BigDecimal;

public record HarvestSummary(
        BigDecimal totalStcg,
        BigDecimal totalLtcg,
        BigDecimal totalEstimatedTax,
        BigDecimal totalExitLoad,
        BigDecimal totalRedemptionValue,
        BigDecimal remainingPortfolioValue) {}
