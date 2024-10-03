package com.app.folioman.portfolio.models.response;

import java.io.Serializable;
import java.util.List;

public record PortfolioResponse(double totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS)
        implements Serializable {}
