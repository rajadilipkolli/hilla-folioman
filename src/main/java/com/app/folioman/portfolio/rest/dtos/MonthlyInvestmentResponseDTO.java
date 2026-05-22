package com.app.folioman.portfolio.rest.dtos;

import com.app.folioman.portfolio.domain.models.projection.MonthlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Serializable DTO wrapper for MonthlyInvestmentResponse interface to support caching.
 */
public record MonthlyInvestmentResponseDTO(
        @Nullable Integer year,
        @Nullable Integer monthNumber,
        @Nullable BigDecimal investmentPerMonth,
        @Nullable BigDecimal cumulativeInvestment)
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
