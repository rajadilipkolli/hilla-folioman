package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

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

            assertThat(props.getAmfi()).isNotNull();
            assertThat(props.getBseStar()).isNotNull();
            assertThat(props.getNav()).isNotNull();

            assertThat(props.getAmfi().getScheme().getDataUrl()).isEqualTo("https://example.com/amfi/scheme");
            assertThat(props.getBseStar().getScheme().getDataUrl()).isEqualTo("https://example.com/bsestar/scheme");
            assertThat(props.getNav().getAmfi().getDataUrl()).isEqualTo("https://example.com/nav/amfi");
            assertThat(props.getNav().getMfApi().getDataUrl()).isEqualTo("https://example.com/nav/mfapi");
        });
    }

    @Test
    void whenPropertiesAreMissing_thenBindingFails() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                // Omit some required properties
                .withPropertyValues("app.amfi.scheme.data-url=https://example.com/amfi/scheme")
                .run(context -> {
                    var ex = assertThatExceptionOfType(Exception.class)
                            .isThrownBy(() -> context.getBean(ApplicationProperties.class))
                            .actual();
                    assertThat(ex.getMessage())
                            .contains(
                                    "startupFailure=org.springframework.boot.context.properties.ConfigurationPropertiesBindException");
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
