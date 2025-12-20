package com.app.folioman.mfschemes.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

/**
 * + * Configuration properties for scheme data URLs.
 * + * This class is used to validate and store URLs for accessing financial scheme data.
 * + */
@Validated
public class SchemeProperties {

    @NotBlank(message = "Data URL must not be blank")
    @Pattern(
            regexp = "^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
            message = "Data URL must be a valid HTTP(S) URL")
    private String dataUrl;

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }
}
