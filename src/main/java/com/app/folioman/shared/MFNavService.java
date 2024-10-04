package com.app.folioman.shared;

import java.time.LocalDate;
import java.util.List;

public interface MFNavService {

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);

    List<Long> getHistoricalDataNotLoadedSchemeIdList();
}
