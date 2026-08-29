package com.app.folioman.mfschemes;

import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MfSchemesAPI {

    private final MFNavService mfNavService;
    private final MfSchemeService mfSchemeService;

    public MfSchemesAPI(MFNavService mfNavService, MfSchemeService mfSchemeService) {
        this.mfNavService = mfNavService;
        this.mfSchemeService = mfSchemeService;
    }

    public MFSchemeDTO getNav(Long schemeCode) {
        return mfNavService.getNav(schemeCode);
    }

    public MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate) {
        return mfNavService.getNavOnDate(schemeCode, inputDate);
    }

    public Map<Long, Map<LocalDate, MFSchemeNavProjection>> getNavsForSchemesAndDates(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate) {
        return mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);
    }

    public Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId) {
        return mfNavService.findTopBySchemeIdOrderByDateDesc(schemeId);
    }

    public List<FundDetailProjection> fetchSchemes(String scheme) {
        return mfSchemeService.fetchSchemes(scheme);
    }

    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        return mfSchemeService.findByPayOut(isin);
    }
}
