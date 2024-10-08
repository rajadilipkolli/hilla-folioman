/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public record UserSchemeDTO(
        String scheme,
        String isin,
        Long amfi,
        String advisor,
        @JsonProperty("rta_code") String rtaCode,
        String type,
        String rta,
        @JsonProperty("open") String myopen,
        String close,
        @JsonProperty("close_calculated") String closeCalculated,
        @JsonProperty("valuation") ValuationDTO valuation,
        @JsonProperty("transactions") List<UserTransactionDTO> transactions)
        implements Serializable {}
