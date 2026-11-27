package com.app.folioman.portfolio.rest.dtos;

import com.app.folioman.portfolio.domain.models.projection.YearlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Serializable DTO wrapper for YearlyInvestmentResponse interface to support caching.
 */
public record YearlyInvestmentResponseDTO(
        @Nullable Integer year, @Nullable BigDecimal yearlyInvestment) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public YearlyInvestmentResponseDTO(YearlyInvestmentResponse source) {
        this(source != null ? source.getYear() : null, source != null ? source.getYearlyInvestment() : null);
    }
}
