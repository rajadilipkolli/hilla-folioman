package com.example.application.mfschemes.models.projection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FundDetailProjection(@NotNull Long schemeId, @NotBlank String schemeName, String fundHouse) {}
