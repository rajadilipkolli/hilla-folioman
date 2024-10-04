package com.app.folioman.mfschemes;

import java.time.LocalDate;
import java.util.List;

public interface MFNavService {

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);

    /**
     * Retrieves a list of scheme IDs for which historical data has not been loaded.
     *
     * @return A List of Long values representing the scheme IDs without loaded historical data.
     */
    List<Long> getHistoricalDataNotLoadedSchemeIdList();
}
