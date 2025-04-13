package com.app.folioman.portfolio.models.response;

import java.math.BigDecimal;

public interface YearlyInvestmentResponse {
    Integer getYear();

    BigDecimal getYearlyInvestment();
}
