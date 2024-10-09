package com.app.folioman.portfolio.models.request;

import com.app.folioman.portfolio.web.validator.ValidSumOfRatios;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

// InvestmentRequest class to hold list of funds and total investment amount
public record InvestmentRequest(
        @NotEmpty(message = "Investment request and funds list cannot be null or empty") @ValidSumOfRatios @Valid
                List<Fund> funds,
        @Positive(message = "Amount to invest cannot be negative") double amountToInvest) {}
