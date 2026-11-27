/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.rest.dtos;

import java.io.Serializable;
import org.jspecify.annotations.Nullable;

public record InvestorInfoDTO(
        @Nullable String email,
        String name,
        @Nullable String mobile,
        @Nullable String address) implements Serializable {}
