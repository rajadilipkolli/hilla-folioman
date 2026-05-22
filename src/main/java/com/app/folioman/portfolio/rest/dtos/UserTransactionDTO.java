/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record UserTransactionDTO(
        @Nullable LocalDate date,
        String description,
        Double amount,
        @Nullable Double units,
        Double nav,
        Double balance,
        TransactionType type,
        @JsonProperty("dividend_rate") @Nullable String dividendRate)
        implements Serializable {}
