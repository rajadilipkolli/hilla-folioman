package com.app.folioman.portfolio.models.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UserPortfolioValueProjection {

    BigDecimal getXirr();

    BigDecimal getLiveXirr();

    BigDecimal getInvested();

    BigDecimal getValue();

    LocalDate getDate();
}
