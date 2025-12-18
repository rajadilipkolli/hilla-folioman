package com.app.folioman.config.db;

import jakarta.validation.Valid;
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

    @Min(value = 5, message = "Min OverGrow size should be 5")
    private int maxOvergrowPoolSize;

    @Valid
    private AcquisitionStrategy acquisitionStrategy;

    @Valid
    private ConnectionLeak connectionLeak = new ConnectionLeak();

    @Valid
    private Metrics metrics = new Metrics();

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

    public ConnectionLeak getConnectionLeak() {
        return connectionLeak;
    }

    public void setConnectionLeak(ConnectionLeak connectionLeak) {
        this.connectionLeak = connectionLeak;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public static class AcquisitionStrategy {

        /** Number of retry attempts for connection acquisition */
        @Min(value = 1, message = "At least one retry must be configured")
        @Max(value = 10, message = "Maximum 10 retries allowed")
        private int retries;

        /** Timeout increment in milliseconds between retries */
        @Min(value = 50, message = "Increment timeout must be at least 50ms")
        private int incrementTimeout;

        @Positive(message = "Lease time threshold must be non-negative")
        private long leaseTimeThreshold;

        @Min(value = 100, message = "Acquisition timeout must be at least 100ms")
        private long acquisitionTimeout;

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

    /**
     * Configuration for connection leak detection
     */
    public static class ConnectionLeak {
        /** Whether leak detection is enabled */
        private boolean enabled = true;

        /** Threshold in milliseconds after which to consider a connection leaked */
        @Min(value = 30000, message = "Leak threshold must be at least 30 seconds")
        private long thresholdMs = 300000; // Default 5 minutes

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getThresholdMs() {
            return thresholdMs;
        }

        public void setThresholdMs(long thresholdMs) {
            this.thresholdMs = thresholdMs;
        }
    }

    /**
     * Configuration for connection pool metrics
     */
    public static class Metrics {
        /** Whether detailed metrics collection is enabled */
        private boolean detailed = true;

        /** Interval in milliseconds for metrics reporting */
        @Min(value = 1000, message = "Metrics reporting interval must be at least 1 second")
        private long reportingIntervalMs = 60000; // Default 1 minute

        public boolean isDetailed() {
            return detailed;
        }

        public void setDetailed(boolean detailed) {
            this.detailed = detailed;
        }

        public long getReportingIntervalMs() {
            return reportingIntervalMs;
        }

        public void setReportingIntervalMs(long reportingIntervalMs) {
            this.reportingIntervalMs = reportingIntervalMs;
        }
    }
}
