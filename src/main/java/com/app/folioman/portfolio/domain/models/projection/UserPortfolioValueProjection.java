package com.app.folioman.portfolio.domain.models.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public interface UserPortfolioValueProjection {

    @Nullable
    BigDecimal getXirr();

    @Nullable
    BigDecimal getLiveXirr();

    BigDecimal getInvested();

    BigDecimal getValue();

    LocalDate getDate();
}
