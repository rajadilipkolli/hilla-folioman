package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingResponseDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@Validated
@Endpoint(id = "capitalGainsHarvesting")
@RolesAllowed("USER")
public class CapitalGainsHarvestingController {

    private final PortfolioAPI portfolioAPI;

    public CapitalGainsHarvestingController(PortfolioAPI portfolioAPI) {
        this.portfolioAPI = portfolioAPI;
    }

    @PostMapping("/{pan}/capital-gains-harvesting")
    public CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(
            @PathVariable("pan") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
                    String pan,
            @RequestBody @Valid CapitalGainsHarvestingRequestDTO request) {
        return portfolioAPI.getCapitalGainsHarvesting(pan, request);
    }
}
