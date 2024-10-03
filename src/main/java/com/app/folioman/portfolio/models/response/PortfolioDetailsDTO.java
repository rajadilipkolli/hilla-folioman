package com.app.folioman.portfolio.models.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record PortfolioDetailsDTO(
        BigDecimal totalValue, String schemeName, String folioNumber, String date, double xirr)
        implements Serializable {

    public PortfolioDetailsDTO(BigDecimal totalValue, String schemeName, String folioNumber, String date, double xirr) {
        if (schemeName == null || schemeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheme name cannot be null or empty");
        }
        if (folioNumber == null || folioNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Folio number cannot be null or empty");
        }
        if (date == null || date.trim().isEmpty()) {
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
