package com.app.folioman.portfolio.models.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing the investment returns for a given PAN.
 */
public record InvestmentReturnsDTO(
        BigDecimal xirr, BigDecimal cagr, BigDecimal invested, BigDecimal currentValue, LocalDate valuationDate)
        implements Serializable {}
