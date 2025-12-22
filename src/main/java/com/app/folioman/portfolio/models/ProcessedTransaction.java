package com.app.folioman.portfolio.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProcessedTransaction(LocalDate date, BigDecimal invested, BigDecimal average, BigDecimal balance) {}
