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

    Map<Long, Map<LocalDate, MFSchemeNavProjection>> getNavsForSchemesAndDates(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate);
}
