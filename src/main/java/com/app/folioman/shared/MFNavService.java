package com.app.folioman.shared;

import java.time.LocalDate;

public interface MFNavService {

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);
}
