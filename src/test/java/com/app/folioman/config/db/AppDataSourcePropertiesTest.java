package com.app.folioman.config.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AppDataSourcePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "app.datasource.maxOvergrowPoolSize=10",
                    "app.datasource.acquisitionStrategy.retries=3",
                    "app.datasource.acquisitionStrategy.incrementTimeout=100",
                    "app.datasource.acquisitionStrategy.leaseTimeThreshold=500",
                    "app.datasource.acquisitionStrategy.acquisitionTimeout=1000",
                    "app.datasource.connectionLeak.enabled=true",
                    "app.datasource.connectionLeak.thresholdMs=60000",
                    "app.datasource.metrics.detailed=true",
                    "app.datasource.metrics.reportingIntervalMs=30000");

    @Test
    void whenPropertiesAreValid_thenBindingsAreCorrect() {
        contextRunner.run(context -> {
            AppDataSourceProperties properties = context.getBean(AppDataSourceProperties.class);

            assertThat(properties.getMaxOvergrowPoolSize()).isEqualTo(10);
            assertThat(properties.getAcquisitionStrategy().getRetries()).isEqualTo(3);
            assertThat(properties.getAcquisitionStrategy().getIncrementTimeout())
                    .isEqualTo(100);
            assertThat(properties.getAcquisitionStrategy().getLeaseTimeThreshold())
                    .isEqualTo(500);
            assertThat(properties.getAcquisitionStrategy().getAcquisitionTimeout())
                    .isEqualTo(1000);
            assertThat(properties.getConnectionLeak().isEnabled()).isTrue();
            assertThat(properties.getConnectionLeak().getThresholdMs()).isEqualTo(60000);
            assertThat(properties.getMetrics().isDetailed()).isTrue();
            assertThat(properties.getMetrics().getReportingIntervalMs()).isEqualTo(30000);
        });
    }

    @Test
    void whenPropertiesAreMissing_thenBindingFails() {
        contextRunner.withPropertyValues("app.datasource.maxOvergrowPoolSize=").run(context -> assertThatThrownBy(
                        () -> context.getBean(AppDataSourceProperties.class))
                .isInstanceOf(Exception.class));
    }

    @Test
    void whenInvalidPropertyValues_thenBindingFails() {
        contextRunner
                .withPropertyValues(
                        "app.datasource.maxOvergrowPoolSize=-1",
                        "app.datasource.acquisitionStrategy.retries=-5",
                        "app.datasource.acquisitionStrategy.incrementTimeout=-100",
                        "app.datasource.connectionLeak.thresholdMs=-60000")
                .run(context -> assertThatThrownBy(() -> context.getBean(AppDataSourceProperties.class))
                        .isInstanceOf(Exception.class));
    }

    @Test
    void whenPartialPropertiesAreProvided_thenDefaultsAreUsed() {
        contextRunner
                .withPropertyValues(
                        "app.datasource.maxOvergrowPoolSize=15", "app.datasource.acquisitionStrategy.retries=2")
                .run(context -> {
                    AppDataSourceProperties properties = context.getBean(AppDataSourceProperties.class);

                    assertThat(properties.getMaxOvergrowPoolSize()).isEqualTo(15);
                    assertThat(properties.getAcquisitionStrategy().getRetries()).isEqualTo(2);
                    assertThat(properties.getAcquisitionStrategy().getIncrementTimeout())
                            .isEqualTo(100); // Default value
                    assertThat(properties.getAcquisitionStrategy().getLeaseTimeThreshold())
                            .isEqualTo(500); // Default value
                });
    }

    @EnableConfigurationProperties(AppDataSourceProperties.class)
    static class TestConfig {}
}
