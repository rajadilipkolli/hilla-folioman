package com.app.folioman.portfolio.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CapitalGainsHarvestingRequest(
        String pan,
        @Nullable LocalDate asOfDate,
        @Nullable String financialYear,
        @Nullable BigDecimal targetAmount,
        @Nullable String taxRegime,
        boolean includeStcg,
        boolean includeLtcg,
        boolean includeExitLoad,
        @Nullable BigDecimal existingRealizedGains,
        @Nullable BigDecimal exemptionOverride,
        @Nullable BigDecimal minRedemptionAmount,
        @Nullable List<String> schemeFilters,
        @Nullable List<String> amcFilters) {}
