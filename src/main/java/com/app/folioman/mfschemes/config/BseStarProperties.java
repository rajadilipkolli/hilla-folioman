package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for BSE Star integration.
 *
 * Example configuration:
 * ```
 * app.bsestar:
 *   scheme:
 *     data-url: https://example.com/bse/schemes
 * ```
 */
@Validated
@ConfigurationProperties(prefix = "app.bsestar")
public class BseStarProperties {

    @Valid
    private SchemeProperties scheme;

    public SchemeProperties getScheme() {
        return scheme;
    }

    public void setScheme(SchemeProperties scheme) {
        this.scheme = scheme;
    }
}
