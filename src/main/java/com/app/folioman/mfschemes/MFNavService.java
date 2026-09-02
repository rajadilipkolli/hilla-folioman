package com.app.folioman.mfschemes;

import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing and retrieving mutual fund NAV (Net Asset Value) data.
 * Provides methods for fetching current and historical NAV information for mutual fund schemes.
 */
public interface MFNavService {

    /**
     * Retrieves the latest NAV for a given scheme code.
     *
     * @param schemeCode The scheme code to retrieve NAV for
     * @return The mutual fund scheme DTO with latest NAV
     */
    MFSchemeDTO getNav(Long schemeCode);

    /**
     * Retrieves the NAV for a scheme on a specific date.
     *
     * @param schemeCode The scheme code to retrieve NAV for
     * @param inputDate The date to retrieve NAV for
     * @return The mutual fund scheme DTO with NAV for the specified date
     */
    MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate);

    /**
     * Retrieves the NAV for a scheme on a specific date with retry logic.
     *
     * @param schemeId The scheme ID to retrieve NAV for
     * @param asOfDate The date to retrieve NAV for
     * @return The mutual fund scheme DTO with NAV for the specified date
     */
    MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate);

    /**
     * Loads the latest day's NAV data for all schemes.
     */
    void loadLastDayDataNav();

    /**
     * Loads historical NAV data if it doesn't already exist in the database.
     */
    void loadHistoricalDataIfNotExists();

    /**
     * Retrieves a mapping of AMFI codes to ISIN codes.
     *
     * @return A map where keys are ISIN codes and values are AMFI codes
     */
    Map<String, Long> getAmfiCodeIsinMap();

    /**
     * Finds the most recent NAV entry for a given scheme ID.
     *
     * @param schemeId The scheme ID to retrieve NAV for
     * @return Optional containing the latest scheme NAV if found, empty otherwise
     */
    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);

    /**
     * Process NAVs for a list of scheme codes asynchronously.
     * This method should handle parallel processing and transactional boundaries.
     *
     * @param schemeCodes List of scheme codes to process
     */
    void processNavsAsync(List<Long> schemeCodes);

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

    /**
     * Retrieves the last two NAVs for a given set of AMFI codes.
     * Batched to avoid N+1 lookups.
     *
     * @param amfiCodes Set of AMFI codes
     * @return Map of AMFI code to a list of the latest two NAV projections
     */
    Map<Long, List<MFSchemeNavProjection>> getLastTwoNavsForSchemes(Set<Long> amfiCodes);
}
