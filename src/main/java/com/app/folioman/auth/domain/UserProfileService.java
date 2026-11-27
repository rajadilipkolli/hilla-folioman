package com.app.folioman.auth.domain;

import com.app.folioman.auth.UserProfileQuery;
import com.app.folioman.auth.rest.dtos.PortfolioSummaryItemDTO;
import com.app.folioman.auth.rest.dtos.PortfoliosDTO;
import com.app.folioman.auth.rest.dtos.UserProfileDTO;
import com.app.folioman.portfolio.PortfolioSummaryQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserProfileService implements UserProfileQuery {

    private final UserRepository userRepository;
    private final PortfolioSummaryQuery portfolioSummaryQuery;

    UserProfileService(UserRepository userRepository, PortfolioSummaryQuery portfolioSummaryQuery) {
        this.userRepository = userRepository;
        this.portfolioSummaryQuery = portfolioSummaryQuery;
    }

    public UserProfileDTO getCurrentUserProfile(String username) {
        UserEntity user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        var portfolioSummaries = portfolioSummaryQuery.getPortfolioSummariesByEmail(user.getEmail());
        var mutualFunds = portfolioSummaries.stream()
                .map(ps -> new PortfolioSummaryItemDTO(ps.getName(), ps.getValue(), ps.getXirr()))
                .toList();

        return new UserProfileDTO(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                new PortfoliosDTO(mutualFunds));
    }
}
