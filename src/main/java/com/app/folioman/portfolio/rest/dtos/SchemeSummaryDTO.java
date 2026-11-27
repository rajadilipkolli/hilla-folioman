package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SchemeSummaryDTO(
        String id,
        String name,
        @Nullable Double xirr,
        CategoryDTO category,
        String rta,
        String plan,
        @Nullable Double nav0,
        @Nullable Double nav1,
        @JsonProperty("nav_date") LocalDate navDate,
        @Nullable Double invested,
        @Nullable Double units,
        @Nullable Double value,
        @JsonProperty("avg_nav") @Nullable Double avgNav,
        ChangeDTO change,
        @JsonProperty("change_pct") ChangeDTO changePct,
        List<FolioSummaryDTO> folios)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
