package com.app.folioman.mfschemes;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface MFNavService {

    MFSchemeDTO getNav(Long schemeCode);

    MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate);

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);

    void loadLastDayDataNav();

    void loadHistoricalDataIfNotExists();

    Map<String, String> getAmfiCodeIsinMap();

    /**
     * Retrieves NAV projections for multiple schemes over a date range.
     *
     * @param schemeCodes Set of scheme codes to retrieve NAVs for
     * @param startDate The start date of the date range (inclusive)
     * @param endDate The end date of the date range (inclusive)
     * @return A map of scheme codes to their NAV projections over the date range
     */
    Map<Long, Map<LocalDate, MFSchemeNavProjection>> getNavsForSchemesAndDates(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate);
}
