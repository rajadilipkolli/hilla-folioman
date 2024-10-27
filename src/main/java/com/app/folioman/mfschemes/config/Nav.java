package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public class Nav {

    private Amfi amfi;

    @Valid
    private MfApi mfApi;

    public Amfi getAmfi() {
        return amfi;
    }

    public void setAmfi(Amfi amfi) {
        this.amfi = amfi;
    }

    public MfApi getMfApi() {
        return mfApi;
    }

    public void setMfApi(MfApi mfApi) {
        this.mfApi = mfApi;
    }
}
