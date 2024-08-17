package com.example.application.mfschemes;

import java.io.Serializable;

public record MFSchemeDTO(
        String amc, Long schemeCode, String isin, String schemeName, String nav, String date, String schemeType)
        implements Serializable {

    public MFSchemeDTO withNavAndDateAndSchemeType(String schemeType, String navValue, String navDate) {
        return new MFSchemeDTO(amc(), schemeCode(), isin(), schemeName(), navValue, navDate, schemeType);
    }
}
