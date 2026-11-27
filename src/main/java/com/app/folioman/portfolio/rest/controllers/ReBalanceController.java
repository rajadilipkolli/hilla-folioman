package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.ReBalanceUseCase;
import com.app.folioman.portfolio.rest.dtos.InvestmentRequest;
import com.app.folioman.portfolio.rest.dtos.InvestmentResponse;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@Validated
@Endpoint
@RolesAllowed("USER")
public class ReBalanceController {

    private final ReBalanceUseCase reBalanceUseCase;

    public ReBalanceController(ReBalanceUseCase reBalanceUseCase) {
        this.reBalanceUseCase = reBalanceUseCase;
    }

    @PostMapping("/rebalance")
    public InvestmentResponse reBalance(@RequestBody @Valid InvestmentRequest investmentRequest) {
        return reBalanceUseCase.reBalance(investmentRequest);
    }
}
