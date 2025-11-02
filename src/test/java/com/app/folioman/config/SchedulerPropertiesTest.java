package com.app.folioman.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerPropertiesTest {

    private SchedulerProperties schedulerProperties;

    @BeforeEach
    void setUp() {
        schedulerProperties = new SchedulerProperties();
    }

    @Test
    void testDefaultAmfiJobCron() {
        assertEquals("*/5 * * * *", schedulerProperties.getAmfiJobCron());
    }

    @Test
    void testSetAndGetAmfiJobCron() {
        String cronExpression = "0 0 12 * * *";
        schedulerProperties.setAmfiJobCron(cronExpression);
        assertEquals(cronExpression, schedulerProperties.getAmfiJobCron());
    }

    @Test
    void testSetAmfiJobCronWithNull() {
        schedulerProperties.setAmfiJobCron(null);
        assertNull(schedulerProperties.getAmfiJobCron());
    }

    @Test
    void testSetAmfiJobCronWithEmptyString() {
        schedulerProperties.setAmfiJobCron("");
        assertEquals("", schedulerProperties.getAmfiJobCron());
    }

    @Test
    void testDefaultHistoricalNavJobCron() {
        assertEquals("*/30 * * * *", schedulerProperties.getHistoricalNavJobCron());
    }

    @Test
    void testSetAndGetHistoricalNavJobCron() {
        String cronExpression = "0 15 10 * * *";
        schedulerProperties.setHistoricalNavJobCron(cronExpression);
        assertEquals(cronExpression, schedulerProperties.getHistoricalNavJobCron());
    }

    @Test
    void testSetHistoricalNavJobCronWithNull() {
        schedulerProperties.setHistoricalNavJobCron(null);
        assertNull(schedulerProperties.getHistoricalNavJobCron());
    }

    @Test
    void testSetHistoricalNavJobCronWithEmptyString() {
        schedulerProperties.setHistoricalNavJobCron("");
        assertEquals("", schedulerProperties.getHistoricalNavJobCron());
    }

    @Test
    void testDefaultDailyDataJobCron() {
        assertEquals("0 0 0 * * *", schedulerProperties.getDailyDataJobCron());
    }

    @Test
    void testSetAndGetDailyDataJobCron() {
        String cronExpression = "0 30 6 * * *";
        schedulerProperties.setDailyDataJobCron(cronExpression);
        assertEquals(cronExpression, schedulerProperties.getDailyDataJobCron());
    }

    @Test
    void testSetDailyDataJobCronWithNull() {
        schedulerProperties.setDailyDataJobCron(null);
        assertNull(schedulerProperties.getDailyDataJobCron());
    }

    @Test
    void testSetDailyDataJobCronWithEmptyString() {
        schedulerProperties.setDailyDataJobCron("");
        assertEquals("", schedulerProperties.getDailyDataJobCron());
    }

    @Test
    void testDefaultAdaptiveStrategyJobCron() {
        assertEquals("*/10 * * * *", schedulerProperties.getAdaptiveStrategyJobCron());
    }

    @Test
    void testSetAndGetAdaptiveStrategyJobCron() {
        String cronExpression = "0 0 */2 * * *";
        schedulerProperties.setAdaptiveStrategyJobCron(cronExpression);
        assertEquals(cronExpression, schedulerProperties.getAdaptiveStrategyJobCron());
    }

    @Test
    void testSetAdaptiveStrategyJobCronWithNull() {
        schedulerProperties.setAdaptiveStrategyJobCron(null);
        assertNull(schedulerProperties.getAdaptiveStrategyJobCron());
    }

    @Test
    void testSetAdaptiveStrategyJobCronWithEmptyString() {
        schedulerProperties.setAdaptiveStrategyJobCron("");
        assertEquals("", schedulerProperties.getAdaptiveStrategyJobCron());
    }

    @Test
    void testDefaultTransactionCacheEvictionCron() {
        assertEquals("0 45 18 * * *", schedulerProperties.getTransactionCacheEvictionCron());
    }

    @Test
    void testSetAndGetTransactionCacheEvictionCron() {
        String cronExpression = "0 0 20 * * *";
        schedulerProperties.setTransactionCacheEvictionCron(cronExpression);
        assertEquals(cronExpression, schedulerProperties.getTransactionCacheEvictionCron());
    }

    @Test
    void testSetTransactionCacheEvictionCronWithNull() {
        schedulerProperties.setTransactionCacheEvictionCron(null);
        assertNull(schedulerProperties.getTransactionCacheEvictionCron());
    }

    @Test
    void testSetTransactionCacheEvictionCronWithEmptyString() {
        schedulerProperties.setTransactionCacheEvictionCron("");
        assertEquals("", schedulerProperties.getTransactionCacheEvictionCron());
    }
}
