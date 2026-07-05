package com.app.folioman.portfolio.domain.models;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CapitalGainsHarvestingRequest(
        String pan,
        @Nullable BigDecimal targetAmount,
        boolean includeStcg,
        boolean includeLtcg,
        boolean includeExitLoad,
        @Nullable BigDecimal exemptionOverride,
        @Nullable BigDecimal minRedemptionAmount,
        @Nullable List<String> schemeFilters,
        @Nullable List<String> amcFilters) {}
