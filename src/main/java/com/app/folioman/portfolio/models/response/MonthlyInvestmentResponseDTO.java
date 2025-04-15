package com.app.folioman.portfolio.models.response;

import com.app.folioman.portfolio.models.projection.MonthlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Serializable DTO wrapper for MonthlyInvestmentResponse interface to support caching.
 */
public record MonthlyInvestmentResponseDTO(
        Integer year, Integer monthNumber, BigDecimal investmentPerMonth, BigDecimal cumulativeInvestment)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public MonthlyInvestmentResponseDTO(MonthlyInvestmentResponse source) {
        this(
                source != null ? source.getYear() : null,
                source != null ? source.getMonthNumber() : null,
                source != null ? source.getInvestmentPerMonth() : null,
                source != null ? source.getCumulativeInvestment() : null);
    }
}
