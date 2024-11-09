package com.app.folioman.config.db;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app.datasource")
@Validated
public class AppDataSourceProperties {

    private int leaseTimeThreshold;
    private int acquisitionTimeout;
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

    public AcquisitionStrategy getAcquisitionStrategy() {
        return acquisitionStrategy;
    }

    public void setAcquisitionStrategy(AcquisitionStrategy acquisitionStrategy) {
        this.acquisitionStrategy = acquisitionStrategy;
    }

    private class AcquisitionStrategy {
        private int retries;
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
