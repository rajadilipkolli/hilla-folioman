package com.app.folioman.portfolio.models.response;

import com.app.folioman.portfolio.models.projection.YearlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Serializable DTO wrapper for YearlyInvestmentResponse interface to support caching.
 */
public record YearlyInvestmentResponseDTO(Integer year, BigDecimal yearlyInvestment) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public YearlyInvestmentResponseDTO(YearlyInvestmentResponse source) {
        this(source != null ? source.getYear() : null, source != null ? source.getYearlyInvestment() : null);
    }
}
