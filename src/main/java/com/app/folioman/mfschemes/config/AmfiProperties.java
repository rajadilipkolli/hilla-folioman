package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for AMFI (Association of Mutual Funds in India) related settings.
 *
 * Example configuration:
 * <pre>
 * app.amfi:
 *   data-url: https://www.amfiindia.com/data
 *   scheme:
 *     # scheme properties here
 * </pre>
 *
 * @property dataUrl The URL to fetch AMFI data
 * @property scheme Configuration for scheme-related settings
 */
@ConfigurationProperties(prefix = "app.amfi")
public class AmfiProperties {

    @Valid
    @NestedConfigurationProperty
    private SchemeProperties scheme;

    private String dataUrl;

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public SchemeProperties getScheme() {
        return scheme;
    }

    public void setScheme(SchemeProperties scheme) {
        this.scheme = scheme;
    }
}
