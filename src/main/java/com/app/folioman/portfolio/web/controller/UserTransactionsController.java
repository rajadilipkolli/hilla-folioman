package com.app.folioman.portfolio.web.controller;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
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
public class UserTransactionsController {

    private final UserTransactionDetailsService userTransactionDetailsService;

    UserTransactionsController(UserTransactionDetailsService userTransactionDetailsService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
    }

    @GetMapping("/investments/{pan}")
    @Cacheable(value = CacheNames.TRANSACTION_CACHE, key = "'monthly_' + #pan")
    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
    }

    @GetMapping("/investments/yearly/{pan}")
    @Cacheable(value = CacheNames.TRANSACTION_CACHE, key = "'yearly_' + #pan")
    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerYear(pan);
    }
}
