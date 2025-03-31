package com.app.folioman.portfolio.models.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

/**
 * Represents an individual fund in an investment portfolio.
 * This record encapsulates the value and ratio of a fund,
 * with built-in validation constraints.
 *
 * The value must be positive, and the ratio must be between 0 and 1 (inclusive).
 */
public record Fund(
        @Positive(message = "Fund value cannot be negative") double value,
        @DecimalMin(value = "0.0", message = "Fund ratio must be between 0 and 1") @DecimalMax(value = "1.0", message = "Fund ratio must be between 0 and 1") double ratio) {}
