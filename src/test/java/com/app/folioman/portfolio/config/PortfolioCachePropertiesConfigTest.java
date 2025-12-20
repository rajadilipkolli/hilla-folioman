package com.app.folioman.portfolio.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class PortfolioCachePropertiesConfigTest {

    @Test
    void shouldHaveConfigurationAnnotation() {
        assertThat(PortfolioCachePropertiesConfig.class.isAnnotationPresent(Configuration.class))
                .isTrue();
    }

    @Test
    void shouldHaveEnableConfigurationPropertiesAnnotation() {
        assertThat(PortfolioCachePropertiesConfig.class.isAnnotationPresent(EnableConfigurationProperties.class))
                .isTrue();
    }

    @Test
    void shouldEnablePortfolioCacheProperties() {
        EnableConfigurationProperties annotation =
                PortfolioCachePropertiesConfig.class.getAnnotation(EnableConfigurationProperties.class);

        assertThat(annotation).isNotNull();
        Class<?>[] value = annotation.value();
        assertThat(value.length).isGreaterThan(0);
        assertThat(java.util.Arrays.asList(value)).contains(PortfolioCacheProperties.class);
    }

    @Test
    void shouldInstantiateSuccessfully() {
        PortfolioCachePropertiesConfig config = new PortfolioCachePropertiesConfig();
        assertThat(config).isNotNull();
    }
}
