package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
class CacheAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheAdapter.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    private CachePolicy currentPolicy;

    CacheAdapter(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Set the cache policy and apply it to existing keys in the cache.
     *
     * @param policy The cache policy to apply.
     */
    public void setPolicy(CachePolicy policy) {
        this.currentPolicy = policy;
        applyPolicy();
    }

    /**
     * Apply the current policy to existing cache entries.
     * This could mean updating expiration times or performing policy-specific actions.
     */
    private void applyPolicy() {
        if (currentPolicy == null) {
            LOGGER.info("No cache policy is set.");
            return;
        }

        LOGGER.info("Applying cache policy: {}", currentPolicy.getClass().getSimpleName());

        // Additional policy-specific behavior can be handled by calling the apply method
        currentPolicy.apply(redisTemplate, meterRegistry);

        LOGGER.info("Cache policy applied to all keys.");
    }
}
