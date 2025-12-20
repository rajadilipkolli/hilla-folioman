package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MfSchemesPropertiesTest {

    private MfSchemesProperties mfSchemesProperties;

    @BeforeEach
    void setUp() {
        mfSchemesProperties = new MfSchemesProperties();
    }

    @Test
    void getBatchSizeDefaultValue() {
        assertThat(mfSchemesProperties.getBatchSize()).isEqualTo(500);
    }

    @Test
    void setBatchSizePositiveValue() {
        mfSchemesProperties.setBatchSize(1000);
        assertThat(mfSchemesProperties.getBatchSize()).isEqualTo(1000);
    }

    @Test
    void setBatchSizeZero() {
        mfSchemesProperties.setBatchSize(0);
        assertThat(mfSchemesProperties.getBatchSize()).isZero();
    }

    @Test
    void setBatchSizeNegativeValue() {
        mfSchemesProperties.setBatchSize(-100);
        assertThat(mfSchemesProperties.getBatchSize()).isEqualTo(-100);
    }

    @Test
    void setBatchSizeLargeValue() {
        mfSchemesProperties.setBatchSize(Integer.MAX_VALUE);
        assertThat(mfSchemesProperties.getBatchSize()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void getRetryAttemptsDefaultValue() {
        assertThat(mfSchemesProperties.getRetryAttempts()).isEqualTo(3);
    }

    @Test
    void setRetryAttemptsPositiveValue() {
        mfSchemesProperties.setRetryAttempts(5);
        assertThat(mfSchemesProperties.getRetryAttempts()).isEqualTo(5);
    }

    @Test
    void setRetryAttemptsZero() {
        mfSchemesProperties.setRetryAttempts(0);
        assertThat(mfSchemesProperties.getRetryAttempts()).isZero();
    }

    @Test
    void setRetryAttemptsNegativeValue() {
        mfSchemesProperties.setRetryAttempts(-1);
        assertThat(mfSchemesProperties.getRetryAttempts()).isEqualTo(-1);
    }

    @Test
    void setRetryAttemptsLargeValue() {
        mfSchemesProperties.setRetryAttempts(Integer.MAX_VALUE);
        assertThat(mfSchemesProperties.getRetryAttempts()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void getRetryDelayMsDefaultValue() {
        assertThat(mfSchemesProperties.getRetryDelayMs()).isEqualTo(1000L);
    }

    @Test
    void setRetryDelayMsPositiveValue() {
        mfSchemesProperties.setRetryDelayMs(2000L);
        assertThat(mfSchemesProperties.getRetryDelayMs()).isEqualTo(2000L);
    }

    @Test
    void setRetryDelayMsZero() {
        mfSchemesProperties.setRetryDelayMs(0L);
        assertThat(mfSchemesProperties.getRetryDelayMs()).isZero();
    }

    @Test
    void setRetryDelayMsNegativeValue() {
        mfSchemesProperties.setRetryDelayMs(-500L);
        assertThat(mfSchemesProperties.getRetryDelayMs()).isEqualTo(-500L);
    }

    @Test
    void setRetryDelayMsLargeValue() {
        mfSchemesProperties.setRetryDelayMs(Long.MAX_VALUE);
        assertThat(mfSchemesProperties.getRetryDelayMs()).isEqualTo(Long.MAX_VALUE);
    }
}
