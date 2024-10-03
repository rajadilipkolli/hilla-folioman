package com.app.folioman.portfolio.models.response;

import java.io.Serializable;

public record PortfolioDetailsDTO(double totalValue, String schemeName, String folioNumber, String date, double xirr)
        implements Serializable {

    public PortfolioDetailsDTO(double totalValue, String schemeName, String folioNumber, String date, double xirr) {
        this.totalValue = totalValue;
        this.schemeName = schemeName;
        this.folioNumber = folioNumber;
        this.date = date;
        this.xirr = xirr * 100;
    }
}
