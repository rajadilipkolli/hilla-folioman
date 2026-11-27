package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.ReBalanceUseCase;
import com.app.folioman.portfolio.rest.dtos.Fund;
import com.app.folioman.portfolio.rest.dtos.InvestmentRequest;
import com.app.folioman.portfolio.rest.dtos.InvestmentResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class ReBalanceServiceImpl implements ReBalanceUseCase {

    @Override
    public InvestmentResponse reBalance(InvestmentRequest investmentRequest) {
        List<Double> investments = new ArrayList<>();

        BigDecimal totalCurrentValue = investmentRequest.funds().stream()
                .map(fund -> BigDecimal.valueOf(fund.value()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAfterInvestment = totalCurrentValue.add(BigDecimal.valueOf(investmentRequest.amountToInvest()));

        for (Fund fund : investmentRequest.funds()) {
            BigDecimal targetValue = BigDecimal.valueOf(fund.ratio()).multiply(totalAfterInvestment);
            BigDecimal amountToInvestInFund = targetValue.subtract(BigDecimal.valueOf(fund.value()));
            investments.add(
                    amountToInvestInFund.setScale(6, RoundingMode.HALF_UP).doubleValue());
        }

        return new InvestmentResponse(investments);
    }
}
