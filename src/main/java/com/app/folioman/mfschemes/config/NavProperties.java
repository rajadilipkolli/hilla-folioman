package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for NAV (Net Asset Value) related settings.
 */
@Validated
public class NavProperties {

    @Valid
    @NestedConfigurationProperty
    private AmfiProperties amfi;

    @Valid
    @NestedConfigurationProperty
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
