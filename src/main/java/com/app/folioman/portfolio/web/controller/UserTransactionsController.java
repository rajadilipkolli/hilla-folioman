package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import java.util.Collections;
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
        if (pan == null || pan.trim().isEmpty()) {
            throw new IllegalArgumentException("PAN cannot be null or empty");
        }
        // Standard Indian PAN format: ABCDE1234F
        if (!pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
            throw new IllegalArgumentException("Invalid PAN format");
        }
        List<MonthlyInvestmentResponse> investments =
                userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
        return investments != null ? investments : Collections.emptyList();
    }
}
