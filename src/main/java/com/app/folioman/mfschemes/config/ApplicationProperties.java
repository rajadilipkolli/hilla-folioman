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
    private Amfi amfi;

    @Valid
    private BseStar bseStar;

    @Valid
    private Nav nav;

    public Amfi getAmfi() {
        return amfi;
    }

    public void setAmfi(Amfi amfi) {
        this.amfi = amfi;
    }

    public BseStar getBseStar() {
        return bseStar;
    }

    public void setBseStar(BseStar bseStar) {
        this.bseStar = bseStar;
    }

    public Nav getNav() {
        return nav;
    }

    public void setNav(Nav nav) {
        this.nav = nav;
    }
}
