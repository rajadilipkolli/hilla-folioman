package com.app.folioman.auth.domain;

import com.app.folioman.auth.rest.dtos.PortfolioSummaryItemDTO;
import com.app.folioman.auth.rest.dtos.PortfoliosDTO;
import com.app.folioman.auth.rest.dtos.UserProfileDTO;
import com.app.folioman.portfolio.PortfolioAPI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final PortfolioAPI portfolioAPI;

    UserProfileService(UserRepository userRepository, PortfolioAPI portfolioAPI) {
        this.userRepository = userRepository;
        this.portfolioAPI = portfolioAPI;
    }

    public UserProfileDTO getCurrentUserProfile(String username) {
        UserEntity user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        var portfolioSummaries = portfolioAPI.getPortfolioSummariesByEmail(user.getEmail());
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
