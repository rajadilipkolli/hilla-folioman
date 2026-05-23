package com.app.folioman.portfolio;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public interface PortfolioSummaryProjection {
    String getName();

    BigDecimal getValue();

    @Nullable
    BigDecimal getXirr();
}
