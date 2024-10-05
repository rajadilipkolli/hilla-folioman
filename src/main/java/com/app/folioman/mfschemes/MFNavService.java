package com.app.folioman.mfschemes;

import java.time.LocalDate;
import java.util.Map;

public interface MFNavService {

    MFSchemeDTO getNav(Long schemeCode);

    MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate);

    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);

    void loadLastDayDataNav();

    void loadHistoricalDataIfNotExists();

    Map<String, String> getAmfiCodeIsinMap();
}
