package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

public record FolioSummaryDTO(
        String folio,
        @Nullable Double invested,
        @Nullable Double units,
        @Nullable Double value,
        @JsonProperty("avg_nav") @Nullable Double avgNav)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
