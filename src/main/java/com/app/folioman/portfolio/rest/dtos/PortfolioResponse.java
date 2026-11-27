package com.app.folioman.portfolio.rest.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(BigDecimal totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS)
        implements Serializable {}
