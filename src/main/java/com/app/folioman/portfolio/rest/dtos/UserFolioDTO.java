/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.rest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record UserFolioDTO(
        String folio,
        String amc,
        @JsonProperty("PAN") @Nullable String pan,
        @JsonProperty("KYC") @Nullable String kyc,
        @JsonProperty("PANKYC") @Nullable String panKyc,
        List<UserSchemeDTO> schemes)
        implements Serializable {}
