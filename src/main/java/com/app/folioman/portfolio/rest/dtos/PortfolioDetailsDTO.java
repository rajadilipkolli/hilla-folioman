package com.app.folioman.portfolio.rest.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

public record PortfolioDetailsDTO(
        BigDecimal totalValue, String schemeName, String folioNumber, String date, double xirr)
        implements Serializable {

    public PortfolioDetailsDTO(
            BigDecimal totalValue,
            @NonNull String schemeName,
            @NonNull String folioNumber,
            @NonNull String date,
            double xirr) {
        if (schemeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheme name cannot be null or empty");
        }
        if (folioNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Folio number cannot be null or empty");
        }
        if (date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }

        this.totalValue = totalValue;
        this.schemeName = schemeName;
        this.folioNumber = folioNumber;
        this.date = date;
        // Multiply xirr by 100 to convert it to a percentage
        this.xirr = xirr * 100;
    }
}
