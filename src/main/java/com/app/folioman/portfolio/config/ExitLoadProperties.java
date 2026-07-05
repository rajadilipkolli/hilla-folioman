package com.app.folioman.portfolio.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for exit load rules.
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app.exit-load")
@Validated
@SuppressWarnings("NullAway.Init")
public class ExitLoadProperties {

    @NotNull
    @PositiveOrZero
    private BigDecimal defaultPercentage;

    @PositiveOrZero
    private int applicabilityWindowDays;

    public BigDecimal getDefaultPercentage() {
        return defaultPercentage;
    }

    public void setDefaultPercentage(BigDecimal defaultPercentage) {
        this.defaultPercentage = defaultPercentage;
    }

    public int getApplicabilityWindowDays() {
        return applicabilityWindowDays;
    }

    public void setApplicabilityWindowDays(int applicabilityWindowDays) {
        this.applicabilityWindowDays = applicabilityWindowDays;
    }
}
