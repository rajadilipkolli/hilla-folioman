package com.app.folioman.mfschemes.domain.dto;

import java.time.LocalDate;

public record LocalApiNavDto(String isin, LocalDate date, java.math.BigDecimal nav) {}
