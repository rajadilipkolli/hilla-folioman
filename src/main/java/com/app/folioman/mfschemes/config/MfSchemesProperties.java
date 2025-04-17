package com.app.folioman.mfschemes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for mutual fund schemes.
 */
@Component
@ConfigurationProperties(prefix = "app.mfschemes")
public class MfSchemesProperties {

    /**
     * Batch size for processing mutual fund schemes.
     */
    private int batchSize = 500;

    /**
     * Number of retry attempts for fetching mutual fund data.
     */
    private int retryAttempts = 3;

    /**
     * Delay in milliseconds between retry attempts.
     */
    private long retryDelayMs = 1000;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}
