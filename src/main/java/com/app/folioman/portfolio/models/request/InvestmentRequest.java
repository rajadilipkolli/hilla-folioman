package com.app.folioman.portfolio.models.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

// InvestmentRequest class to hold list of funds and total investment amount
public record InvestmentRequest(
        @NotEmpty List<Fund> funds, @Positive(message = "Amount to invest cannot be negative") double amountToInvest) {}
