package com.app.folioman.mfschemes.domain.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Data transfer object containing metadata about a mutual fund scheme.
 * Includes fund house, scheme type, category, code, and name information.
 *
 * @param fundHouse The name of the fund house
 * @param schemeType The type of the scheme
 * @param schemeCategory The category of the scheme
 * @param schemeCode The unique code of the scheme
 * @param schemeName The name of the scheme
 */
public record MetaDTO(
        @JsonProperty("fund_house") String fundHouse,
        @JsonProperty("scheme_type") String schemeType,
        @JsonProperty("scheme_category") String schemeCategory,
        @JsonProperty("scheme_code") String schemeCode,
        @JsonProperty("scheme_name") String schemeName)
        implements Serializable {}
