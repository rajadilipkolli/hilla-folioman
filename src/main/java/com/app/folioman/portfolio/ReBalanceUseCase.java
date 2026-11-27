package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.InvestmentRequest;
import com.app.folioman.portfolio.rest.dtos.InvestmentResponse;

public interface ReBalanceUseCase {

    InvestmentResponse reBalance(InvestmentRequest investmentRequest);
}
