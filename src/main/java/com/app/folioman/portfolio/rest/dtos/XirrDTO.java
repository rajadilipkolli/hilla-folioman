package com.app.folioman.portfolio.rest.dtos;

import java.io.Serial;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

public record XirrDTO(@Nullable Double current, @Nullable Double overall) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
