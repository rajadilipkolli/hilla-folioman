package com.example.application.config.redis;

import org.springframework.stereotype.Component;

/**
 * A factory class for creating different caching policies based on a provided strategy string.
 */
@Component
public class PolicyRepository {

    /**
     * Returns a caching policy based on the provided strategy string.
     *
     * @param strategy the strategy string used to determine which caching policy to instantiate
     * @return a caching policy based on the provided strategy string
     */
    public CachePolicy getPolicy(String strategy) {
        if (strategy == null || strategy.isEmpty()) {
            throw new IllegalArgumentException("Strategy cannot be null or empty");
        }
        return switch (strategy) {
            case "REDUCE_CACHE_SIZE" -> new ReduceCacheSizePolicy();
            case "INCREASE_CACHE_SIZE" -> new IncreaseCacheSizePolicy();
            case "ADJUST_TTL" -> new AdjustTTLPolicy();
            default -> new DefaultPolicy();
        };
    }
}
