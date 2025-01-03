package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ApplicationPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "app.amfi.scheme.data-url=https://example.com/amfi/scheme",
                    "app.bsestar.scheme.data-url=https://example.com/bsestar/scheme",
                    "app.nav.amfi.data-url=https://example.com/nav/amfi",
                    "app.nav.mfapi.data-url=https://example.com/nav/mfapi");

    @Test
    void whenPropertiesAreValid_thenBindingsAreCorrect() {
        contextRunner.run(context -> {
            ApplicationProperties props = context.getBean(ApplicationProperties.class);

            assertNotNull(props.getAmfi());
            assertNotNull(props.getBseStar());
            assertNotNull(props.getNav());

            assertEquals(
                    "https://example.com/amfi/scheme",
                    props.getAmfi().getScheme().getDataUrl());
            assertEquals(
                    "https://example.com/bsestar/scheme",
                    props.getBseStar().getScheme().getDataUrl());
            assertEquals(
                    "https://example.com/nav/amfi", props.getNav().getAmfi().getDataUrl());
            assertEquals(
                    "https://example.com/nav/mfapi", props.getNav().getMfApi().getDataUrl());
        });
    }

    @Test
    void whenPropertiesAreMissing_thenBindingFails() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                // Omit some required properties
                .withPropertyValues("app.amfi.scheme.data-url=https://example.com/amfi/scheme")
                .run(context -> {
                    var ex = assertThrows(Exception.class, () -> context.getBean(ApplicationProperties.class));
                    assertTrue(
                            ex.getMessage()
                                    .contains(
                                            "startupFailure=org.springframework.boot.context.properties.ConfigurationPropertiesBindException"));
                });
    }

    @EnableConfigurationProperties({ApplicationProperties.class, BseStarProperties.class, MfApiProperties.class})
    @TestConfiguration
    static class TestConfig {
        // This class enables the ApplicationProperties to be loaded into the context
        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
