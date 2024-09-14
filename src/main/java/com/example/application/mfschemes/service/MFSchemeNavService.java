package com.example.application.mfschemes.service;

import com.example.application.mfschemes.NavNotFoundException;
import com.example.application.mfschemes.models.response.MFSchemeDTO;
import com.example.application.mfschemes.util.SchemeConstants;
import com.example.application.shared.LocalDateUtility;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class MFSchemeNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFSchemeNavService.class);
    private final CachedNavService cachedNavService;
    private final MfSchemeInternalService mfSchemeService;
    private final MfHistoricalNavService historicalNavService;

    public MFSchemeNavService(
            CachedNavService cachedNavService,
            MfSchemeInternalService mfSchemeService,
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
}
