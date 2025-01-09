package com.app.folioman.mfschemes.web.controller;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AnonymousAllowed
@Endpoint
@RestController
@RequestMapping("/api/nav")
@Validated
class NAVController {

    private final MFNavService mfNavService;

    public NAVController(MFNavService mfNavService) {
        this.mfNavService = mfNavService;
    }

    @GetMapping(path = "/{schemeCode}")
    MFSchemeDTO getScheme(
            @PathVariable
                    @Min(value = 100000, message = "Min value of schemeCode should be greater than 100000") @Max(value = 160000, message = "Max value of schemeCode should be less than 160000") @Valid Long schemeCode) {
        return mfNavService.getNav(schemeCode);
    }

    @GetMapping(path = "/{schemeCode}/{date}")
    public MFSchemeDTO getSchemeNavOnDate(
            @PathVariable
                    @Min(value = 100000, message = "Min value of schemeCode should be greater than 100000") @Max(value = 160000, message = "Max value of schemeCode should be less than 160000") @Valid Long schemeCode,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inputDate) {
        return mfNavService.getNavOnDate(schemeCode, inputDate);
    }
}
