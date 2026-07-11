package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import com.app.folioman.shared.EmailAware;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.RolesAllowed;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    private final PortfolioAPI portfolioAPI;

    public PortfolioHistoryController(PortfolioAPI portfolioAPI) {
        this.portfolioAPI = portfolioAPI;
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<PortfolioHistoryDTO> getPortfolioHistory(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {

        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusYears(1) : from;
        String userEmail = extractUserEmail(principal);

        return portfolioAPI
                .getPortfolioHistory(id, userEmail, effectiveFrom, effectiveTo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String extractUserEmail(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object authPrincipal = authentication.getPrincipal();
            if (authPrincipal instanceof UserDetails userDetails) {
                if (authPrincipal instanceof EmailAware emailAware) {
                    return emailAware.getEmail();
                }
                return userDetails.getUsername();
            }
            if (authPrincipal instanceof OidcUser oidcUser) {
                String email = oidcUser.getEmail();
                return email != null ? email : "";
            }
            if (authPrincipal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                return email != null ? email : "";
            }
        }
        return principal != null && principal.getName() != null ? principal.getName() : "";
    }
}
