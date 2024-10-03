package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.LocalDateUtility;
import com.app.folioman.shared.MFNavService;
import com.app.folioman.shared.MFSchemeDTO;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class MFSchemeNavService implements MFNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFSchemeNavService.class);
    private final CachedNavService cachedNavService;
    private final MfSchemesService mfSchemeService;
    private final MfHistoricalNavService historicalNavService;

    MFSchemeNavService(
            CachedNavService cachedNavService,
            MfSchemesService mfSchemeService,
            MfHistoricalNavService historicalNavService) {
        this.cachedNavService = cachedNavService;
        this.mfSchemeService = mfSchemeService;
        this.historicalNavService = historicalNavService;
    }

    public MFSchemeDTO getNav(Long schemeCode) {
        return getNavByDateWithRetry(schemeCode, LocalDateUtility.getAdjustedDate());
    }

    public MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate) {
        LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(inputDate);
        return getNavByDateWithRetry(schemeCode, adjustedDate);
    }

    @Override
    public MFSchemeDTO getNavByDateWithRetry(Long schemeCode, LocalDate navDate) {
        LOGGER.info("Fetching Nav for AMFISchemeCode: {} for date: {} from Cache", schemeCode, navDate);
        MFSchemeDTO mfSchemeDTO;
        int retryCount = 0;

        while (true) {
            try {
                mfSchemeDTO = cachedNavService.getNavForDate(schemeCode, navDate);
                break; // Exit the loop if successful
            } catch (NavNotFoundException navNotFoundException) {
                LOGGER.error("NavNotFoundException occurred: {}", navNotFoundException.getMessage());

                LocalDate currentNavDate = navNotFoundException.getDate();
                if (retryCount == SchemeConstants.FIRST_RETRY || retryCount == SchemeConstants.THIRD_RETRY) {
                    // make a call to get historical Data and persist
                    String oldSchemeCode = historicalNavService.getHistoricalNav(schemeCode, navDate);
                    if (StringUtils.hasText(oldSchemeCode)) {
                        mfSchemeService.fetchSchemeDetails(oldSchemeCode, schemeCode);
                        currentNavDate = LocalDateUtility.getAdjustedDate(currentNavDate.plusDays(2));
                    } else {
                        // NFO scenario where data is not present in historical data, hence load all available data
                        mfSchemeService.fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
                    }
                }
                // retrying 4 times
                if (retryCount >= SchemeConstants.MAX_RETRIES) {
                    throw navNotFoundException;
                }

                retryCount++;
                navDate = LocalDateUtility.getAdjustedDate(currentNavDate.minusDays(1));
                LOGGER.info("Retrying for date: {} for scheme: {}", navDate, schemeCode);
            }
        }
        return mfSchemeDTO;
    }

    @Override
    public Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId) {
        return Optional.ofNullable(getNav(schemeId));
    }
}
