package com.app.folioman.mfschemes.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.nav.mfapi")
public class MfApiProperties {

    @NotBlank(message = "Data URL must not be blank") @Pattern(regexp = "^https?://.*", message = "Data URL must be a valid HTTP(S) URL")
    private String dataUrl;

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }
}
