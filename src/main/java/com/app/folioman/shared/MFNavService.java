package com.app.folioman.shared;

import java.util.Optional;
import java.time.LocalDate;

public interface MFNavService {

    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);
}
