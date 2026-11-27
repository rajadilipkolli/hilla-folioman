package com.app.folioman.mfschemes;

import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import java.util.List;
import java.util.Optional;

public interface MfSchemeService {

    Optional<MFSchemeProjection> findByPayOut(String isin);

    Optional<MFSchemeProjection> findByAmfiCode(Long amfiCode);

    List<MFSchemeProjection> findByAmfiCodeIn(List<Long> amfiCodes);

    List<FundDetailProjection> fetchSchemes(String scheme);
}
