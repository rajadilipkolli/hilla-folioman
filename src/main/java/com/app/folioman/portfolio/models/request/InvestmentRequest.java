package com.app.folioman.portfolio.models.request;

import java.util.List;

// InvestmentRequest class to hold list of funds and total investment amount
public record InvestmentRequest(List<Fund> funds, double amountToInvest) {}
