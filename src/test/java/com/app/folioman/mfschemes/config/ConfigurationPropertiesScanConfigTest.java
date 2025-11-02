package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

class ConfigurationPropertiesScanConfigTest {

    @Test
    void shouldInstantiateConfigurationClass() {
        ConfigurationPropertiesScanConfig config = new ConfigurationPropertiesScanConfig();
        assertNotNull(config);
    }

    @Test
    void shouldHaveConfigurationPropertiesScanAnnotation() {
        boolean hasAnnotation =
                ConfigurationPropertiesScanConfig.class.isAnnotationPresent(ConfigurationPropertiesScan.class);
        assertTrue(hasAnnotation);
    }
}
