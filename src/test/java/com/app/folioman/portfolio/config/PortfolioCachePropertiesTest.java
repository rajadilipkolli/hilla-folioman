package com.app.folioman.portfolio.config;

import static org.assertj.core.api.Assertions.assertThat;

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
    void defaultConstructor() {
        assertThat(portfolioCacheProperties.getEviction()).isNotNull();
    }

    @Test
    void getEviction() {
        PortfolioCacheProperties.Eviction defaultEviction = portfolioCacheProperties.getEviction();
        assertThat(defaultEviction).isNotNull();
        assertThat(defaultEviction.getBatchSize()).isEqualTo(100);
        assertThat(defaultEviction.getTransactionCron()).isEqualTo("0 45 18 * * *");
    }

    @Test
    void setEviction() {
        PortfolioCacheProperties.Eviction newEviction = new PortfolioCacheProperties.Eviction();
        newEviction.setBatchSize(200);
        newEviction.setTransactionCron("0 30 19 * * *");

        portfolioCacheProperties.setEviction(newEviction);

        assertThat(portfolioCacheProperties.getEviction()).isEqualTo(newEviction);
        assertThat(portfolioCacheProperties.getEviction().getBatchSize()).isEqualTo(200);
        assertThat(portfolioCacheProperties.getEviction().getTransactionCron()).isEqualTo("0 30 19 * * *");
    }

    @Test
    void setEvictionWithNull() {
        portfolioCacheProperties.setEviction(null);
        assertThat(portfolioCacheProperties.getEviction()).isNull();
    }

    @Test
    void evictionDefaultValues() {
        assertThat(eviction.getBatchSize()).isEqualTo(100);
        assertThat(eviction.getTransactionCron()).isEqualTo("0 45 18 * * *");
    }

    @Test
    void evictionGetBatchSize() {
        assertThat(eviction.getBatchSize()).isEqualTo(100);
    }

    @Test
    void evictionSetBatchSize() {
        eviction.setBatchSize(250);
        assertThat(eviction.getBatchSize()).isEqualTo(250);
    }

    @Test
    void evictionSetBatchSizeWithZero() {
        eviction.setBatchSize(0);
        assertThat(eviction.getBatchSize()).isZero();
    }

    @Test
    void evictionSetBatchSizeWithNegativeValue() {
        eviction.setBatchSize(-50);
        assertThat(eviction.getBatchSize()).isEqualTo(-50);
    }

    @Test
    void evictionGetTransactionCron() {
        assertThat(eviction.getTransactionCron()).isEqualTo("0 45 18 * * *");
    }

    @Test
    void evictionSetTransactionCron() {
        String newCron = "0 0 12 * * *";
        eviction.setTransactionCron(newCron);
        assertThat(eviction.getTransactionCron()).isEqualTo(newCron);
    }

    @Test
    void evictionSetTransactionCronWithNull() {
        eviction.setTransactionCron(null);
        assertThat(eviction.getTransactionCron()).isNull();
    }

    @Test
    void evictionSetTransactionCronWithEmptyString() {
        eviction.setTransactionCron("");
        assertThat(eviction.getTransactionCron()).isEmpty();
    }

    @Test
    void evictionConstructor() {
        PortfolioCacheProperties.Eviction newEviction = new PortfolioCacheProperties.Eviction();
        assertThat(newEviction.getBatchSize()).isEqualTo(100);
        assertThat(newEviction.getTransactionCron()).isEqualTo("0 45 18 * * *");
    }
}
