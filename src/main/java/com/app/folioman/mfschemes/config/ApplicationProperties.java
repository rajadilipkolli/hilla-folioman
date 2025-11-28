package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Application-wide configuration properties.
 * Configure using 'app.*' properties in application.properties/yaml.
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    @Valid
    private AmfiProperties amfi;

    @Valid
    private BseStarProperties bseStar;

    @Valid
    private NavProperties nav;

    public AmfiProperties getAmfi() {
        return amfi;
    }

    public void setAmfi(@Valid AmfiProperties amfi) {
        this.amfi = amfi;
    }

    public BseStarProperties getBseStar() {
        return bseStar;
    }

    public void setBseStar(@Valid BseStarProperties bseStar) {
        this.bseStar = bseStar;
    }

    public NavProperties getNav() {
        return nav;
    }

    public void setNav(@Valid NavProperties nav) {
        this.nav = nav;
    }
}
