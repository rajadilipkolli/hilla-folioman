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

    @Min(value = 5, message = "Min OverGrow size should be 5") private int maxOvergrowPoolSize;

    private AcquisitionStrategy acquisitionStrategy;

    public int getMaxOvergrowPoolSize() {
        return maxOvergrowPoolSize;
    }

    public void setMaxOvergrowPoolSize(int maxOvergrowPoolSize) {
        this.maxOvergrowPoolSize = maxOvergrowPoolSize;
    }

    public AcquisitionStrategy getAcquisitionStrategy() {
        return acquisitionStrategy;
    }

    public void setAcquisitionStrategy(AcquisitionStrategy acquisitionStrategy) {
        this.acquisitionStrategy = acquisitionStrategy;
    }

    public static class AcquisitionStrategy {

        /** Number of retry attempts for connection acquisition */
        @Min(value = 1, message = "At least one retry must be configured") @Max(value = 10, message = "Maximum 10 retries allowed") private int retries;

        /** Timeout increment in milliseconds between retries */
        @Min(value = 50, message = "Increment timeout must be at least 50ms") private int incrementTimeout;

        @Positive(message = "Lease time threshold must be non-negative") private long leaseTimeThreshold;

        @Min(value = 100, message = "Acquisition timeout must be at least 100ms") private long acquisitionTimeout;

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

        public long getLeaseTimeThreshold() {
            return leaseTimeThreshold;
        }

        public void setLeaseTimeThreshold(long leaseTimeThreshold) {
            this.leaseTimeThreshold = leaseTimeThreshold;
        }

        public long getAcquisitionTimeout() {
            return acquisitionTimeout;
        }

        public void setAcquisitionTimeout(long acquisitionTimeout) {
            this.acquisitionTimeout = acquisitionTimeout;
        }
    }
}
