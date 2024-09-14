package com.example.application.mfschemes.controller;

import com.example.application.mfschemes.service.MfSchemeServiceImpl;
import com.example.application.shared.FundDetailProjection;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AnonymousAllowed
@Endpoint
@RestController
@RequestMapping("/api/scheme")
public class SchemeController {

    private final MfSchemeServiceImpl mfSchemeService;

    public SchemeController(MfSchemeServiceImpl mfSchemeService) {
        this.mfSchemeService = mfSchemeService;
    }

    @GetMapping(path = "/{schemeName}")
    public List<FundDetailProjection> fetchSchemes(@PathVariable String schemeName) {
        return mfSchemeService.fetchSchemes(schemeName);
    }
}
