package com.app.folioman.portfolio.models.response;

import java.math.BigDecimal;

public interface MonthlyInvestmentResponse {
    Integer getYear();

    Integer getMonthNumber();

    BigDecimal getInvestmentPerMonth();

    BigDecimal getCumulativeInvestment();
}
