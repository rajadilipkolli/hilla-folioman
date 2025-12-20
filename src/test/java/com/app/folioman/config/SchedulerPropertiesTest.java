package com.app.folioman.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerPropertiesTest {

    private SchedulerProperties schedulerProperties;

    @BeforeEach
    void setUp() {
        schedulerProperties = new SchedulerProperties();
    }

    @Test
    void defaultAmfiJobCron() {
        assertThat(schedulerProperties.getAmfiJobCron()).isEqualTo("*/5 * * * *");
    }

    @Test
    void setAndGetAmfiJobCron() {
        String cronExpression = "0 0 12 * * *";
        schedulerProperties.setAmfiJobCron(cronExpression);
        assertThat(schedulerProperties.getAmfiJobCron()).isEqualTo(cronExpression);
    }

    @Test
    void setAmfiJobCronWithNull() {
        schedulerProperties.setAmfiJobCron(null);
        assertThat(schedulerProperties.getAmfiJobCron()).isNull();
    }

    @Test
    void setAmfiJobCronWithEmptyString() {
        schedulerProperties.setAmfiJobCron("");
        assertThat(schedulerProperties.getAmfiJobCron()).isEmpty();
    }

    @Test
    void defaultHistoricalNavJobCron() {
        assertThat(schedulerProperties.getHistoricalNavJobCron()).isEqualTo("*/30 * * * *");
    }

    @Test
    void setAndGetHistoricalNavJobCron() {
        String cronExpression = "0 15 10 * * *";
        schedulerProperties.setHistoricalNavJobCron(cronExpression);
        assertThat(schedulerProperties.getHistoricalNavJobCron()).isEqualTo(cronExpression);
    }

    @Test
    void setHistoricalNavJobCronWithNull() {
        schedulerProperties.setHistoricalNavJobCron(null);
        assertThat(schedulerProperties.getHistoricalNavJobCron()).isNull();
    }

    @Test
    void setHistoricalNavJobCronWithEmptyString() {
        schedulerProperties.setHistoricalNavJobCron("");
        assertThat(schedulerProperties.getHistoricalNavJobCron()).isEmpty();
    }

    @Test
    void defaultDailyDataJobCron() {
        assertThat(schedulerProperties.getDailyDataJobCron()).isEqualTo("0 0 0 * * *");
    }

    @Test
    void setAndGetDailyDataJobCron() {
        String cronExpression = "0 30 6 * * *";
        schedulerProperties.setDailyDataJobCron(cronExpression);
        assertThat(schedulerProperties.getDailyDataJobCron()).isEqualTo(cronExpression);
    }

    @Test
    void setDailyDataJobCronWithNull() {
        schedulerProperties.setDailyDataJobCron(null);
        assertThat(schedulerProperties.getDailyDataJobCron()).isNull();
    }

    @Test
    void setDailyDataJobCronWithEmptyString() {
        schedulerProperties.setDailyDataJobCron("");
        assertThat(schedulerProperties.getDailyDataJobCron()).isEmpty();
    }

    @Test
    void defaultAdaptiveStrategyJobCron() {
        assertThat(schedulerProperties.getAdaptiveStrategyJobCron()).isEqualTo("*/10 * * * *");
    }

    @Test
    void setAndGetAdaptiveStrategyJobCron() {
        String cronExpression = "0 0 */2 * * *";
        schedulerProperties.setAdaptiveStrategyJobCron(cronExpression);
        assertThat(schedulerProperties.getAdaptiveStrategyJobCron()).isEqualTo(cronExpression);
    }

    @Test
    void setAdaptiveStrategyJobCronWithNull() {
        schedulerProperties.setAdaptiveStrategyJobCron(null);
        assertThat(schedulerProperties.getAdaptiveStrategyJobCron()).isNull();
    }

    @Test
    void setAdaptiveStrategyJobCronWithEmptyString() {
        schedulerProperties.setAdaptiveStrategyJobCron("");
        assertThat(schedulerProperties.getAdaptiveStrategyJobCron()).isEmpty();
    }

    @Test
    void defaultTransactionCacheEvictionCron() {
        assertThat(schedulerProperties.getTransactionCacheEvictionCron()).isEqualTo("0 45 18 * * *");
    }

    @Test
    void setAndGetTransactionCacheEvictionCron() {
        String cronExpression = "0 0 20 * * *";
        schedulerProperties.setTransactionCacheEvictionCron(cronExpression);
        assertThat(schedulerProperties.getTransactionCacheEvictionCron()).isEqualTo(cronExpression);
    }

    @Test
    void setTransactionCacheEvictionCronWithNull() {
        schedulerProperties.setTransactionCacheEvictionCron(null);
        assertThat(schedulerProperties.getTransactionCacheEvictionCron()).isNull();
    }

    @Test
    void setTransactionCacheEvictionCronWithEmptyString() {
        schedulerProperties.setTransactionCacheEvictionCron("");
        assertThat(schedulerProperties.getTransactionCacheEvictionCron()).isEmpty();
    }
}
