package com.app.folioman.portfolio.domain.models.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public interface PortfolioValueDateProjection {
    BigDecimal getValue();

    LocalDate getDate();

    @Nullable
    BigDecimal getXirr();

    @Nullable
    BigDecimal getLiveXirr();
}
