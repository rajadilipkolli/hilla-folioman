package com.app.folioman.mfschemes.domain;

import com.github.rajadilipkolli.dailynav.MutualFundService;
import com.github.rajadilipkolli.dailynav.model.Nav;
import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service providing fallback access to mutual fund data when primary sources are unavailable.
 * Delegates to an external mutual fund service for retrieving NAV and scheme information.
 */
@Service
public class MutualFundFallbackService {

    private final MutualFundService mutualFundService;

    MutualFundFallbackService(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }

    /**
     * Retrieves the NAV history for a mutual fund scheme by its scheme code.
     *
     * @param schemeCode The scheme code to retrieve NAVs for
     * @return List of NAV entries for the specified scheme
     */
    public List<Nav> getNavsBySchemeCode(Integer schemeCode) {
        return mutualFundService.getNavsBySchemeCode(schemeCode);
    }

    /**
     * Retrieves the scheme details by scheme code.
     *
     * @param schemeCode The scheme code to retrieve
     * @return Optional containing the scheme if found, empty otherwise
     */
    public Optional<Scheme> getSchemeBySchemeCode(Integer schemeCode) {
        return mutualFundService.getScheme(schemeCode);
    }
}
