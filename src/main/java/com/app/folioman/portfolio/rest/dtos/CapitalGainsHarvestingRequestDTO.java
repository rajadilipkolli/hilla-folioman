package com.app.folioman.portfolio.rest.dtos;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CapitalGainsHarvestingRequestDTO(
        @PastOrPresent(message = "As-of date cannot be in the future")
        LocalDate asOfDate,

        @Nullable String financialYear,

        @Nullable @PositiveOrZero(message = "Target amount cannot be negative")
        BigDecimal targetAmount,

        @Nullable String taxRegime,

        @Nullable @PositiveOrZero(message = "Minimum redemption amount cannot be negative")
        BigDecimal minRedemptionAmount,

        boolean includeExitLoad,
        boolean includeStcg,
        boolean includeLtcg,
        @Nullable List<String> schemeFilters,
        @Nullable List<String> amcFilters,

        @Nullable @PositiveOrZero(message = "Existing realized gains cannot be negative")
        BigDecimal existingRealizedGains,

        @Nullable @PositiveOrZero(message = "Exemption override cannot be negative")
        BigDecimal exemptionOverride) {}
