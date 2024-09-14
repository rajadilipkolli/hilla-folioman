package com.example.application.config.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheAdapter.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private CachePolicy currentPolicy;

    @Autowired
    public CacheAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get a value from the cache.
     *
     * @param key The cache key.
     * @param type The class type of the value.
     * @param <T> The type parameter.
     * @return The value from the cache or null if not found.
     */
    public <T> T get(String key, Class<T> type) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * Put a value into the cache using the current cache policy's expiration time.
     *
     * @param key The cache key.
     * @param value The value to cache.
     * @param <T> The type parameter.
     */
    public <T> void put(String key, T value) {
        redisTemplate.opsForValue().set(key, value, currentPolicy.getExpirationTime());
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
        currentPolicy.apply(redisTemplate);

        LOGGER.info("Cache policy applied to all keys.");
    }
}
