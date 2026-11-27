package com.app.folioman.mfschemes.domain.dto;

public record LocalApiSchemeDto(
        Integer schemeCode, String schemeName, String amc, String category, String plan, String option) {}
