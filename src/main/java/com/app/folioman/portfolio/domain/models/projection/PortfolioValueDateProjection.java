package com.app.folioman.portfolio.domain.models.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PortfolioValueDateProjection {
    BigDecimal getValue();

    LocalDate getDate();

    BigDecimal getXirr();
}
