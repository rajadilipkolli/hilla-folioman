package com.app.folioman.auth.rest.dtos;

import java.io.Serializable;
import java.math.BigDecimal;

public record PortfolioSummaryItemDTO(String name, BigDecimal value, BigDecimal xirr) implements Serializable {}
