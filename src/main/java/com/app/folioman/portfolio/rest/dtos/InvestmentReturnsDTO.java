package com.app.folioman.portfolio.rest.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * DTO representing the investment returns for a given PAN.
 */
public record InvestmentReturnsDTO(
        @Nullable BigDecimal xirr,
        BigDecimal cagr,
        BigDecimal invested,
        BigDecimal currentValue,
        LocalDate valuationDate)
        implements Serializable {}
