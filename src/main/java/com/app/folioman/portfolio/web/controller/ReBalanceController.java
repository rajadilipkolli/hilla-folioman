package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.request.Fund;
import com.app.folioman.portfolio.models.request.InvestmentRequest;
import com.app.folioman.portfolio.models.response.InvestmentResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@Endpoint
@AnonymousAllowed // Allow anonymous access for this endpoint
public class ReBalanceController {

    @PostMapping("/rebalance")
    public InvestmentResponse reBalance(@RequestBody InvestmentRequest investmentRequest) {
        List<Double> investments = new ArrayList<>();

        // Calculate the total current value of all funds
        double totalCurrentValue =
                investmentRequest.funds().stream().mapToDouble(Fund::value).sum();

        // Calculate the total value after the new investment
        double totalAfterInvestment = totalCurrentValue + investmentRequest.amountToInvest();

        // Calculate the target value for each fund and how much to invest
        for (Fund fund : investmentRequest.funds()) {
            double targetValue = fund.ratio() * totalAfterInvestment;
            double amountToInvestInFund = targetValue - fund.value();
            investments.add(amountToInvestInFund);
        }

        return new InvestmentResponse(investments);
    }
}
