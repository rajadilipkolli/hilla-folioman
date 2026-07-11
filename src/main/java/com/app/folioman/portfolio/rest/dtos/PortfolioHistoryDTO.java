package com.app.folioman.portfolio.rest.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record PortfolioHistoryDTO(List<Number[]> invested, List<Number[]> value) implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;
}
