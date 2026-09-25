package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import com.app.folioman.shared.PrincipalUtil;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Endpoint
@RestController
@RequestMapping("/api/portfolio")
@RolesAllowed("USER")
@Validated
public class UserTransactionsController {

    private final PortfolioAPI portfolioAPI;

    public UserTransactionsController(PortfolioAPI portfolioAPI) {
        this.portfolioAPI = portfolioAPI;
    }

    @GetMapping("/returns/{pan}")
    public @Nullable InvestmentReturnsDTO getInvestmentReturns(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        String email = PrincipalUtil.getEmailFromContext();
        if (!portfolioAPI.isPanOwnedByEmail(pan, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return portfolioAPI.getInvestmentReturnsByPan(pan, email).orElse(null);
    }

    @GetMapping("/investments/{pan}")
    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        String email = PrincipalUtil.getEmailFromContext();
        if (!portfolioAPI.isPanOwnedByEmail(pan, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return portfolioAPI.getTotalInvestmentsByPanPerMonth(pan, email);
    }

    @GetMapping("/investments/yearly/{pan}")
    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(
            @PathVariable @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String pan) {
        String email = PrincipalUtil.getEmailFromContext();
        if (!portfolioAPI.isPanOwnedByEmail(pan, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return portfolioAPI.getTotalInvestmentsByPanPerYear(pan, email);
    }
}
