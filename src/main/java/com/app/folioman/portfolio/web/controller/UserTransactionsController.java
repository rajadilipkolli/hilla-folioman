package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponse;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Endpoint
@RestController
@RequestMapping("/api/portfolio")
@AnonymousAllowed
@Validated
class UserTransactionsController {

    private final UserTransactionDetailsService userTransactionDetailsService;

    UserTransactionsController(UserTransactionDetailsService userTransactionDetailsService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
    }

    @GetMapping("/investments/{pan}")
    public List<MonthlyInvestmentResponse> getTotalInvestmentsByPanPerMonth(
            @PathVariable("pan") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format") String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
    }

    @GetMapping("/investments/yearly/{pan}")
    public List<YearlyInvestmentResponse> getTotalInvestmentsByPanPerYear(
            @PathVariable("pan") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format") String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerYear(pan);
    }
}
