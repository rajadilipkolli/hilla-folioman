package com.app.folioman.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app.cache")
public class RedisAppProperties {

    private boolean compressionEnabled;

    private long defaultTtl;

    @NestedConfigurationProperty
    AdaptiveStrategy adaptiveStrategy = new AdaptiveStrategy();

    public AdaptiveStrategy getAdaptiveStrategy() {
        return adaptiveStrategy;
    }

    public void setAdaptiveStrategy(AdaptiveStrategy adaptiveStrategy) {
        this.adaptiveStrategy = adaptiveStrategy;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public long getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(long defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public static class AdaptiveStrategy {

        private long intervalMs = 600000;

        private int stabilityThreshold = 3;

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        public int getStabilityThreshold() {
            return stabilityThreshold;
        }

        public void setStabilityThreshold(int stabilityThreshold) {
            this.stabilityThreshold = stabilityThreshold;
        }
    }
}
