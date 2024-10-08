/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;

public record UserTransactionDTO(
        LocalDate date,
        String description,
        Double amount,
        Double units,
        Double nav,
        Double balance,
        TransactionType type,
        @JsonProperty("dividend_rate") String dividendRate)
        implements Serializable {}
