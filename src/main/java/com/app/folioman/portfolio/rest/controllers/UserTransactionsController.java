package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.jspecify.annotations.Nullable;
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

    private final PortfolioAPI portfolioAPI;

    UserTransactionsController(PortfolioAPI portfolioAPI) {
        this.portfolioAPI = portfolioAPI;
    }

    @GetMapping("/returns/{pan}")
    @Cacheable(value = CacheNames.RETURNS_CACHE, key = "'returns_' + #pan", unless = "#result == null")
    public @Nullable InvestmentReturnsDTO getInvestmentReturns(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        return portfolioAPI.getInvestmentReturnsByPan(pan).orElse(null);
    }

    @GetMapping("/investments/{pan}")
    @Cacheable(value = CacheNames.TRANSACTION_CACHE, key = "'monthly_' + #pan")
    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        return portfolioAPI.getTotalInvestmentsByPanPerMonth(pan);
    }

    @GetMapping("/investments/yearly/{pan}")
    @Cacheable(value = CacheNames.TRANSACTION_CACHE, key = "'yearly_' + #pan")
    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        return portfolioAPI.getTotalInvestmentsByPanPerYear(pan);
    }
}
