package com.example.application.mfschemes.service;

import com.example.application.mfschemes.NavNotFoundException;
import com.example.application.mfschemes.models.response.MFSchemeDTO;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedNavService.class);

    private final MfSchemeInternalService mfSchemeService;

    public CachedNavService(MfSchemeInternalService mfSchemeService) {
        this.mfSchemeService = mfSchemeService;
    }

    @Cacheable(cacheNames = "getNavForDate", unless = "#result == null")
    public MFSchemeDTO getNavForDate(Long schemeCode, LocalDate navDate) {
        LOGGER.info("Fetching Nav for AMFISchemeCode: {} for date: {} from Database", schemeCode, navDate);
        return mfSchemeService
                .getMfSchemeDTO(schemeCode, navDate)
                .orElseGet(() -> fetchAndGetSchemeDetails(schemeCode, navDate));
    }

    MFSchemeDTO fetchAndGetSchemeDetails(Long schemeCode, LocalDate navDate) {
        LOGGER.info("Fetching Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        mfSchemeService.fetchSchemeDetails(schemeCode);
        LOGGER.info("Fetched Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        return mfSchemeService
                .getMfSchemeDTO(schemeCode, navDate)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for schemeCode - " + schemeCode, navDate));
    }
}
