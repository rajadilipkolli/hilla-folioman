package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public class NavProperties {

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
