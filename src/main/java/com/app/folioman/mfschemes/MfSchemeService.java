package com.app.folioman.mfschemes;

import java.util.List;
import java.util.Optional;

public interface MfSchemeService {

    Optional<MFSchemeProjection> findByPayOut(String isin);

    List<FundDetailProjection> fetchSchemes(String scheme);

    void fetchSchemeDetails(Long schemeId);

    List<MFSchemeProjection> fetchSchemesByRtaCode(String rtaCode);
}
