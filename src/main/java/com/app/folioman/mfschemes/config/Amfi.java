package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AMFI (Association of Mutual Funds in India) related settings.
 */
@ConfigurationProperties(prefix = "app.amfi")
public class Amfi {

    @Valid
    private Scheme scheme;

    private String dataUrl;

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }
}
