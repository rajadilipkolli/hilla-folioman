package com.app.folioman.portfolio.rest.dtos;

import java.io.Serial;
import java.io.Serializable;

public record CategoryDTO(String main, String sub) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
