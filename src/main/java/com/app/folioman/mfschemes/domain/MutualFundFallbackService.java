package com.app.folioman.mfschemes.domain;

import com.github.rajadilipkolli.dailynav.MutualFundService;
import com.github.rajadilipkolli.dailynav.model.Nav;
import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MutualFundFallbackService {

    private final MutualFundService mutualFundService;

    MutualFundFallbackService(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }

    public List<Nav> getNavsBySchemeCode(Integer schemeCode) {
        return mutualFundService.getNavsBySchemeCode(schemeCode);
    }

    public Optional<Scheme> getSchemeBySchemeCode(Integer schemeCode) {
        return mutualFundService.getScheme(schemeCode);
    }
}
