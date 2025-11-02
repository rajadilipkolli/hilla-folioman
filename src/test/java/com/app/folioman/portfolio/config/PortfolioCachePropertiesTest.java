package com.app.folioman.portfolio.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioCachePropertiesTest {

    private PortfolioCacheProperties portfolioCacheProperties;
    private PortfolioCacheProperties.Eviction eviction;

    @BeforeEach
    void setUp() {
        portfolioCacheProperties = new PortfolioCacheProperties();
        eviction = new PortfolioCacheProperties.Eviction();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(portfolioCacheProperties.getEviction());
    }

    @Test
    void testGetEviction() {
        PortfolioCacheProperties.Eviction defaultEviction = portfolioCacheProperties.getEviction();
        assertNotNull(defaultEviction);
        assertEquals(100, defaultEviction.getBatchSize());
        assertEquals("0 45 18 * * *", defaultEviction.getTransactionCron());
    }

    @Test
    void testSetEviction() {
        PortfolioCacheProperties.Eviction newEviction = new PortfolioCacheProperties.Eviction();
        newEviction.setBatchSize(200);
        newEviction.setTransactionCron("0 30 19 * * *");

        portfolioCacheProperties.setEviction(newEviction);

        assertEquals(newEviction, portfolioCacheProperties.getEviction());
        assertEquals(200, portfolioCacheProperties.getEviction().getBatchSize());
        assertEquals("0 30 19 * * *", portfolioCacheProperties.getEviction().getTransactionCron());
    }

    @Test
    void testSetEvictionWithNull() {
        portfolioCacheProperties.setEviction(null);
        assertNull(portfolioCacheProperties.getEviction());
    }

    @Test
    void testEvictionDefaultValues() {
        assertEquals(100, eviction.getBatchSize());
        assertEquals("0 45 18 * * *", eviction.getTransactionCron());
    }

    @Test
    void testEvictionGetBatchSize() {
        assertEquals(100, eviction.getBatchSize());
    }

    @Test
    void testEvictionSetBatchSize() {
        eviction.setBatchSize(250);
        assertEquals(250, eviction.getBatchSize());
    }

    @Test
    void testEvictionSetBatchSizeWithZero() {
        eviction.setBatchSize(0);
        assertEquals(0, eviction.getBatchSize());
    }

    @Test
    void testEvictionSetBatchSizeWithNegativeValue() {
        eviction.setBatchSize(-50);
        assertEquals(-50, eviction.getBatchSize());
    }

    @Test
    void testEvictionGetTransactionCron() {
        assertEquals("0 45 18 * * *", eviction.getTransactionCron());
    }

    @Test
    void testEvictionSetTransactionCron() {
        String newCron = "0 0 12 * * *";
        eviction.setTransactionCron(newCron);
        assertEquals(newCron, eviction.getTransactionCron());
    }

    @Test
    void testEvictionSetTransactionCronWithNull() {
        eviction.setTransactionCron(null);
        assertNull(eviction.getTransactionCron());
    }

    @Test
    void testEvictionSetTransactionCronWithEmptyString() {
        eviction.setTransactionCron("");
        assertEquals("", eviction.getTransactionCron());
    }

    @Test
    void testEvictionConstructor() {
        PortfolioCacheProperties.Eviction newEviction = new PortfolioCacheProperties.Eviction();
        assertEquals(100, newEviction.getBatchSize());
        assertEquals("0 45 18 * * *", newEviction.getTransactionCron());
    }
}
