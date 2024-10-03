package com.app.folioman.shared;

import java.time.LocalDate;
import java.util.Optional;

public interface MFNavService {

    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);
}
