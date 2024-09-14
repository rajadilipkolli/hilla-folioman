package com.example.application.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FundDetailProjection(@NotNull Long schemeId, @NotBlank String schemeName, String fundHouse) {}
