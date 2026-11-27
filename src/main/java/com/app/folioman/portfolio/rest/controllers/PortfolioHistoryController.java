package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.PortfolioSummaryQuery;
import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioSummaryDTO;
import com.app.folioman.shared.PrincipalUtil;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Endpoint
@RestController
@RequestMapping("/api/mutualfunds/portfolio")
@RolesAllowed("USER")
@Validated
public class PortfolioHistoryController {

    private final PortfolioSummaryQuery portfolioSummaryQuery;

    public PortfolioHistoryController(PortfolioSummaryQuery portfolioSummaryQuery) {
        this.portfolioSummaryQuery = portfolioSummaryQuery;
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<PortfolioHistoryDTO> getPortfolioHistory(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusYears(1) : from;
        String userEmail = PrincipalUtil.getEmailFromContext();

        return portfolioSummaryQuery
                .getPortfolioHistory(id, userEmail, effectiveFrom, effectiveTo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary(@PathVariable Long id) {
        String userEmail = PrincipalUtil.getEmailFromContext();
        return portfolioSummaryQuery
                .getPortfolioSummary(id, userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
