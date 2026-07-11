package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record PortfolioSummaryDTO(
        @Nullable Double invested,
        @Nullable Double value,
        @JsonProperty("xirr") XirrDTO xirr,
        ChangeDTO change,
        @JsonProperty("change_pct") ChangeDTO changePct,
        LocalDate date,
        List<SchemeSummaryDTO> schemes)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
