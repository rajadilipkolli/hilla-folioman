package com.app.folioman.auth.rest.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record PortfolioSummaryItemDTO(
        String name, BigDecimal value, @Nullable BigDecimal xirr) implements Serializable {}
