package com.app.folioman.auth.rest.dtos;

import java.io.Serializable;
import java.util.List;

public record PortfoliosDTO(List<PortfolioSummaryItemDTO> mutualfunds) implements Serializable {}
