package com.app.folioman.portfolio.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class PortfolioCachePropertiesConfigTest {

    @Test
    void shouldHaveConfigurationAnnotation() {
        assertTrue(PortfolioCachePropertiesConfig.class.isAnnotationPresent(Configuration.class));
    }

    @Test
    void shouldHaveEnableConfigurationPropertiesAnnotation() {
        assertTrue(PortfolioCachePropertiesConfig.class.isAnnotationPresent(EnableConfigurationProperties.class));
    }

    @Test
    void shouldEnablePortfolioCacheProperties() {
        EnableConfigurationProperties annotation =
                PortfolioCachePropertiesConfig.class.getAnnotation(EnableConfigurationProperties.class);

        assertNotNull(annotation);
        Class<?>[] value = annotation.value();
        assertTrue(value.length > 0);
        assertTrue(java.util.Arrays.asList(value).contains(PortfolioCacheProperties.class));
    }

    @Test
    void shouldInstantiateSuccessfully() {
        PortfolioCachePropertiesConfig config = new PortfolioCachePropertiesConfig();
        assertNotNull(config);
    }
}
