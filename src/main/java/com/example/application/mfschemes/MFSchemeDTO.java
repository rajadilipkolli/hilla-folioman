package com.example.application.mfschemes;

public record MFSchemeDTO(
        String amc, Long schemeCode, String isin, String schemeName, String nav, String date, String schemeType) {}
