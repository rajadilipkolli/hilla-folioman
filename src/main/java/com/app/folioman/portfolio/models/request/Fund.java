package com.app.folioman.portfolio.models.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

// Fund class to hold individual fund details
public record Fund(
        @Positive(message = "Fund value cannot be negative") double value,
        @DecimalMin(value = "0.0", message = "Fund ratio must be between 0 and 100")
                @DecimalMax(value = "100.0", message = "Fund ratio must be between 0 and 100")
                double ratio) {}
