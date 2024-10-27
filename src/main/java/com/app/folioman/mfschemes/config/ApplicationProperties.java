package com.app.folioman.mfschemes.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "AMFI configuration is required")
    private AmfiProperties amfi;

    @Valid
    @NotBlank(message = "BSE Star configuration is required")
    private BseStarProperties bseStar;

    @Valid
    @NotBlank(message = "NAV configuration is required")
    private NavProperties nav;

    public AmfiProperties getAmfi() {
        return amfi;
    }

    public void setAmfi(AmfiProperties amfi) {
        this.amfi = amfi;
    }

    public BseStarProperties getBseStar() {
        return bseStar;
    }

    public void setBseStar(BseStarProperties bseStar) {
        this.bseStar = bseStar;
    }

    public NavProperties getNav() {
        return nav;
    }

    public void setNav(NavProperties nav) {
        this.nav = nav;
    }
}
