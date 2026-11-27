package com.app.folioman.portfolio.rest.dtos;

import java.math.BigDecimal;

public record HarvestSummaryDTO(
        BigDecimal totalStcg,
        BigDecimal totalLtcg,
        BigDecimal totalEstimatedTax,
        BigDecimal totalExitLoad,
        BigDecimal totalRedemptionAmount,
        BigDecimal totalRemainingValue) {}
