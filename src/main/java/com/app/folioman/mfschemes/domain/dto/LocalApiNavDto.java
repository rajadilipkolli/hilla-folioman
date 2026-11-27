package com.app.folioman.mfschemes.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LocalApiNavDto(String isin, LocalDate date, BigDecimal nav) {}
