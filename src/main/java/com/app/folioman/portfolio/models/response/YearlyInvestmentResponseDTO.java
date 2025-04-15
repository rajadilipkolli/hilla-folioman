package com.app.folioman.portfolio.models.response;

import com.app.folioman.portfolio.models.projection.YearlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Serializable DTO wrapper for YearlyInvestmentResponse interface to support caching.
 */
public class YearlyInvestmentResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer year;
    private BigDecimal yearlyInvestment;

    public YearlyInvestmentResponseDTO() {
        // Default constructor for serialization
    }

    public YearlyInvestmentResponseDTO(YearlyInvestmentResponse source) {
        if (source != null) {
            this.year = source.getYear();
            this.yearlyInvestment = source.getYearlyInvestment();
        }
    }

    public YearlyInvestmentResponseDTO(int year, BigDecimal yearlyInvestment) {
        this.year = year;
        this.yearlyInvestment = yearlyInvestment;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getYearlyInvestment() {
        return yearlyInvestment;
    }

    public void setYearlyInvestment(BigDecimal yearlyInvestment) {
        this.yearlyInvestment = yearlyInvestment;
    }
}
