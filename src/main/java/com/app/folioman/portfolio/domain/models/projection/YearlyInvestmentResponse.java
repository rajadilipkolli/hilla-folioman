package com.app.folioman.portfolio.domain.models.projection;

import java.math.BigDecimal;

public interface YearlyInvestmentResponse {
    Integer getYear();

    BigDecimal getYearlyInvestment();
}
