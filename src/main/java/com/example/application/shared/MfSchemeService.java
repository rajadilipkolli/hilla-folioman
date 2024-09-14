package com.example.application.shared;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MfSchemeService {

    Optional<MFSchemeProjection> findByPayOut(String isin);

    List<FundDetailProjection> fetchSchemes(String scheme);

    Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate);

    void fetchSchemeDetails(Long schemeCode);
}
