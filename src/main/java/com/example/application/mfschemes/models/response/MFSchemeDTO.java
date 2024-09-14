package com.example.application.mfschemes.models.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

public record MFSchemeDTO(
        String amc,
        @Positive Long schemeCode,
        String isin,
        @NotBlank String schemeName,
        String nav,
        String date,
        String schemeType)
        implements Serializable {

    public MFSchemeDTO withNavAndDateAndSchemeType(String schemeType, String navValue, String navDate) {
        return new MFSchemeDTO(amc(), schemeCode(), isin(), schemeName(), navValue, navDate, schemeType);
    }
}
