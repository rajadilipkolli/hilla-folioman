package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

public record ChangeDTO(
        @JsonProperty("D") @Nullable Double d,
        @JsonProperty("A") @Nullable Double a) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
