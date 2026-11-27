package com.app.folioman.config.redis;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

@SuppressWarnings("NullAway")
public class CustomRedisCache extends RedisCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRedisCache.class);
    private final Monitor monitor;
    private final CacheCircuitBreaker circuitBreaker;

    public CustomRedisCache(
            String name,
            RedisCacheWriter cacheWriter,
            RedisCacheConfiguration cacheConfiguration,
            Monitor monitor,
            CacheCircuitBreaker circuitBreaker) {
        super(name, cacheWriter, cacheConfiguration);
        this.monitor = monitor;
        this.circuitBreaker = circuitBreaker;
    }

    public CustomRedisCache(
            String name,
            RedisCacheWriter cacheWriter,
            RedisSerializer<Object> valueSerializer,
            Duration ttl,
            Monitor monitor,
            CacheCircuitBreaker circuitBreaker) {
        this(name, cacheWriter, RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl), monitor, circuitBreaker);
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        try {
            // Use circuit breaker to handle Redis connection failures
            circuitBreaker.<@Nullable Void>execute(() -> {
                super.put(key, value);
                return null;
            });

            // Custom logic after the put operation
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Put operation completed for key: {}", key);
            }

            monitor.recordUpdate(getName());
        } catch (Exception e) {
            LOGGER.warn("Failed to put key {} in Redis cache: {}", key, e.getMessage());
        }
    }

    @Override
    public Cache.@Nullable ValueWrapper get(Object key) {
        try {
            ValueWrapper valueWrapper =
                    circuitBreaker.<Cache.@Nullable ValueWrapper>executeWithFallback(() -> super.get(key), () -> null);

            monitor.recordAccess(getName());

            if (valueWrapper == null) {
                monitor.recordMiss(getName());
            } else {
                monitor.recordHit(getName());
            }

            return valueWrapper;
        } catch (Exception e) {
            LOGGER.warn("Failed to get key {} from Redis cache: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        try {
            circuitBreaker.<@Nullable Void>execute(() -> {
                super.evict(key);
                return null;
            });

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Evicted key {} from cache", key);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to evict key {} from Redis cache: {}", key, e.getMessage());
        }
    }

    @Override
    public void clear() {
        try {
            // Use circuit breaker for Redis clear
            circuitBreaker.<@Nullable Void>execute(() -> {
                super.clear();
                return null;
            });

            LOGGER.info("Cache {} cleared", getName());
        } catch (Exception e) {
            LOGGER.warn("Failed to clear Redis cache {}: {}", getName(), e.getMessage());
        }
    }
}
