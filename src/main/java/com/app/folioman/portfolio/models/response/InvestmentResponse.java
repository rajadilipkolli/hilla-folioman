package com.app.folioman.portfolio.models.response;

import java.util.List;

// InvestmentResponse class to return list of investment amounts for each fund
public record InvestmentResponse(List<Double> investments) {}
