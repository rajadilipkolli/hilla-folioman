package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MfSchemesPropertiesTest {

    private MfSchemesProperties mfSchemesProperties;

    @BeforeEach
    void setUp() {
        mfSchemesProperties = new MfSchemesProperties();
    }

    @Test
    void testGetBatchSize_DefaultValue() {
        assertEquals(500, mfSchemesProperties.getBatchSize());
    }

    @Test
    void testSetBatchSize_PositiveValue() {
        mfSchemesProperties.setBatchSize(1000);
        assertEquals(1000, mfSchemesProperties.getBatchSize());
    }

    @Test
    void testSetBatchSize_Zero() {
        mfSchemesProperties.setBatchSize(0);
        assertEquals(0, mfSchemesProperties.getBatchSize());
    }

    @Test
    void testSetBatchSize_NegativeValue() {
        mfSchemesProperties.setBatchSize(-100);
        assertEquals(-100, mfSchemesProperties.getBatchSize());
    }

    @Test
    void testSetBatchSize_LargeValue() {
        mfSchemesProperties.setBatchSize(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, mfSchemesProperties.getBatchSize());
    }

    @Test
    void testGetRetryAttempts_DefaultValue() {
        assertEquals(3, mfSchemesProperties.getRetryAttempts());
    }

    @Test
    void testSetRetryAttempts_PositiveValue() {
        mfSchemesProperties.setRetryAttempts(5);
        assertEquals(5, mfSchemesProperties.getRetryAttempts());
    }

    @Test
    void testSetRetryAttempts_Zero() {
        mfSchemesProperties.setRetryAttempts(0);
        assertEquals(0, mfSchemesProperties.getRetryAttempts());
    }

    @Test
    void testSetRetryAttempts_NegativeValue() {
        mfSchemesProperties.setRetryAttempts(-1);
        assertEquals(-1, mfSchemesProperties.getRetryAttempts());
    }

    @Test
    void testSetRetryAttempts_LargeValue() {
        mfSchemesProperties.setRetryAttempts(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, mfSchemesProperties.getRetryAttempts());
    }

    @Test
    void testGetRetryDelayMs_DefaultValue() {
        assertEquals(1000L, mfSchemesProperties.getRetryDelayMs());
    }

    @Test
    void testSetRetryDelayMs_PositiveValue() {
        mfSchemesProperties.setRetryDelayMs(2000L);
        assertEquals(2000L, mfSchemesProperties.getRetryDelayMs());
    }

    @Test
    void testSetRetryDelayMs_Zero() {
        mfSchemesProperties.setRetryDelayMs(0L);
        assertEquals(0L, mfSchemesProperties.getRetryDelayMs());
    }

    @Test
    void testSetRetryDelayMs_NegativeValue() {
        mfSchemesProperties.setRetryDelayMs(-500L);
        assertEquals(-500L, mfSchemesProperties.getRetryDelayMs());
    }

    @Test
    void testSetRetryDelayMs_LargeValue() {
        mfSchemesProperties.setRetryDelayMs(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, mfSchemesProperties.getRetryDelayMs());
    }
}
