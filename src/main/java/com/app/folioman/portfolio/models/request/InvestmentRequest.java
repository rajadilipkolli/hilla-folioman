package com.app.folioman.portfolio.models.request;

import com.app.folioman.portfolio.web.validator.ValidSumOfRatios;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Represents an investment request containing a list of funds and the total amount to invest.
 * This record is used as a data transfer object for processing rebalance calculations.
 * The funds list must not be empty, and the investment amount must be positive.
 */
public record InvestmentRequest(
        @NotEmpty(message = "Investment request and funds list cannot be null or empty") @ValidSumOfRatios @Valid
                List<Fund> funds,
        @PositiveOrZero(message = "Amount to invest cannot be negative") Double amountToInvest) {}
