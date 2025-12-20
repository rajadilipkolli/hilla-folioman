package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

class ConfigurationPropertiesScanConfigTest {

    @Test
    void shouldInstantiateConfigurationClass() {
        ConfigurationPropertiesScanConfig config = new ConfigurationPropertiesScanConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void shouldHaveConfigurationPropertiesScanAnnotation() {
        boolean hasAnnotation =
                ConfigurationPropertiesScanConfig.class.isAnnotationPresent(ConfigurationPropertiesScan.class);
        assertThat(hasAnnotation).isTrue();
    }
}
