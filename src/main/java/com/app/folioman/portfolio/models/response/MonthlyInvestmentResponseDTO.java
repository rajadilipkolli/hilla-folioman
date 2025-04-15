package com.app.folioman.portfolio.models.response;

import com.app.folioman.portfolio.models.projection.MonthlyInvestmentResponse;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Serializable DTO wrapper for MonthlyInvestmentResponse interface to support caching.
 */
public class MonthlyInvestmentResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer year;
    private Integer monthNumber;
    private BigDecimal investmentPerMonth;
    private BigDecimal cumulativeInvestment;

    public MonthlyInvestmentResponseDTO() {
        // Default constructor for serialization
    }

    public MonthlyInvestmentResponseDTO(MonthlyInvestmentResponse source) {
        if (source != null) {
            this.year = source.getYear();
            this.monthNumber = source.getMonthNumber();
            this.investmentPerMonth = source.getInvestmentPerMonth();
            this.cumulativeInvestment = source.getCumulativeInvestment();
        }
    }

    public MonthlyInvestmentResponseDTO(
            int year, int monthNumber, BigDecimal investmentPerMonth, BigDecimal cumulativeInvestment) {
        this.year = year;
        this.monthNumber = monthNumber;
        this.investmentPerMonth = investmentPerMonth;
        this.cumulativeInvestment = cumulativeInvestment;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }

    public BigDecimal getInvestmentPerMonth() {
        return investmentPerMonth;
    }

    public void setInvestmentPerMonth(BigDecimal investmentPerMonth) {
        this.investmentPerMonth = investmentPerMonth;
    }

    public BigDecimal getCumulativeInvestment() {
        return cumulativeInvestment;
    }

    public void setCumulativeInvestment(BigDecimal cumulativeInvestment) {
        this.cumulativeInvestment = cumulativeInvestment;
    }
}
