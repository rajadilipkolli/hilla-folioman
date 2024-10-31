package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Endpoint
@RestController
@RequestMapping("/api/portfolio")
@AnonymousAllowed
class UserTransactionsController {

    private final UserTransactionDetailsService userTransactionDetailsService;

    UserTransactionsController(UserTransactionDetailsService userTransactionDetailsService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
    }

    @GetMapping("/investments/{pan}")
    public List<MonthlyInvestmentResponse> getTotalInvestmentsByPanPerMonth(@PathVariable String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
    }
}
