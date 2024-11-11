package com.app.folioman.config.db;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app.datasource")
@Validated
public class AppDataSourceProperties {

    @Positive(message = "Lease time threshold must be non-negative") private int leaseTimeThreshold;

    @Min(value = 50, message = "Acquisition timeout must be at least 100ms")
    private int acquisitionTimeout;

    @Min(value = 2, message = "Min OverFlow size should be 2")
    private int maxOverflowPoolSize;

    private AcquisitionStrategy acquisitionStrategy;

    public int getLeaseTimeThreshold() {
        return leaseTimeThreshold;
    }

    public void setLeaseTimeThreshold(int leaseTimeThreshold) {
        this.leaseTimeThreshold = leaseTimeThreshold;
    }

    public int getAcquisitionTimeout() {
        return acquisitionTimeout;
    }

    public void setAcquisitionTimeout(int acquisitionTimeout) {
        this.acquisitionTimeout = acquisitionTimeout;
    }

    public int getMaxOverflowPoolSize() {
        return maxOverflowPoolSize;
    }

    public void setMaxOverflowPoolSize(int maxOverflowPoolSize) {
        this.maxOverflowPoolSize = maxOverflowPoolSize;
    }

    public AcquisitionStrategy getAcquisitionStrategy() {
        return acquisitionStrategy;
    }

    public void setAcquisitionStrategy(AcquisitionStrategy acquisitionStrategy) {
        this.acquisitionStrategy = acquisitionStrategy;
    }

    private class AcquisitionStrategy {

        /** Number of retry attempts for connection acquisition */
        @Min(value = 1, message = "At least one retry must be configured")
        @Max(value = 10, message = "Maximum 10 retries allowed")
        private int retries;

        /** Timeout increment in milliseconds between retries */
        @Min(value = 50, message = "Increment timeout must be at least 50ms")
        private int incrementTimeout;

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public int getIncrementTimeout() {
            return incrementTimeout;
        }

        public void setIncrementTimeout(int incrementTimeout) {
            this.incrementTimeout = incrementTimeout;
        }
    }
}
