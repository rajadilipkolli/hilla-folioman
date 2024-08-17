package com.example.application.mfschemes.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

record MetaDTO(
        @JsonProperty("fund_house") String fundHouse,
        @JsonProperty("scheme_type") String schemeType,
        @JsonProperty("scheme_category") String schemeCategory,
        @JsonProperty("scheme_code") String schemeCode,
        @JsonProperty("scheme_name") String schemeName)
        implements Serializable {}
