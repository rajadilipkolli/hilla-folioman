package com.app.folioman.portfolio.domain.models.projection;

import java.math.BigDecimal;

public interface MonthlyInvestmentResponse {
    Integer getYear();

    Integer getMonthNumber();

    BigDecimal getInvestmentPerMonth();

    BigDecimal getCumulativeInvestment();
}
