package com.app.folioman.mfschemes;

import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import java.util.List;
import java.util.Optional;

public interface MfSchemeService {

    Optional<MFSchemeProjection> findByPayOut(String isin);

    List<FundDetailProjection> fetchSchemes(String scheme);

    void fetchSchemeDetails(Long schemeId);

    void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode);

    List<MFSchemeProjection> fetchSchemesByRtaCode(String rtaCode);
}
