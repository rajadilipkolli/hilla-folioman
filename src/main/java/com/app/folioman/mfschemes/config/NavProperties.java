package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for NAV (Net Asset Value) related settings.
 */
@Validated
public class NavProperties {

    @Valid
    private AmfiProperties amfi;

    @Valid
    private MfApiProperties mfApi;

    public AmfiProperties getAmfi() {
        return amfi;
    }

    public void setAmfi(AmfiProperties amfi) {
        this.amfi = amfi;
    }

    public MfApiProperties getMfApi() {
        return mfApi;
    }

    public void setMfApi(MfApiProperties mfApi) {
        this.mfApi = mfApi;
    }
}
