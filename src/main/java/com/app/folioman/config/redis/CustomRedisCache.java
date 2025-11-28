package com.app.folioman.config.redis;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

public class CustomRedisCache extends RedisCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRedisCache.class);
    private final Monitor monitor;
    private final CacheCircuitBreaker circuitBreaker;

    // In-memory fallback cache for critical items when Redis is unavailable
    private final Map<Object, Object> localCache = new ConcurrentHashMap<>();
    // Limit local cache size to prevent memory issues
    private static final int MAX_LOCAL_CACHE_SIZE = 1000;

    public CustomRedisCache(
            String name,
            RedisCacheWriter cacheWriter,
            RedisSerializer<Object> valueSerializer,
            Duration ttl,
            Monitor monitor,
            CacheCircuitBreaker circuitBreaker) {
        super(name, cacheWriter, RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl));
        this.monitor = monitor;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void put(Object key, Object value) {
        try {
            // Use circuit breaker to handle Redis connection failures
            circuitBreaker.execute(() -> {
                super.put(key, value);
                return null;
            });

            // Custom logic after the put operation
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Put operation completed for key: {}", key);
            }

            // Store in local backup cache if Redis was successful and local cache isn't too large
            if (localCache.size() < MAX_LOCAL_CACHE_SIZE) {
                localCache.put(key, value);
            }

            // Record metrics
            recordMetrics(key, "update");
        } catch (Exception e) {
            LOGGER.warn("Failed to put key {} in Redis cache: {}", key, e.getMessage());
            // Still store in local cache as fallback
            if (localCache.size() < MAX_LOCAL_CACHE_SIZE) {
                localCache.put(key, value);
            }
        }
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        try {
            // Try to get from Redis with circuit breaker protection
            ValueWrapper valueWrapper = circuitBreaker.executeWithFallback(() -> super.get(key), () -> {
                // Fallback to local cache if Redis is unavailable
                Object value = localCache.get(key);
                return value != null ? new SimpleValueWrapper(value) : null;
            });

            // Record access metrics
            recordMetrics(key, "access");

            // If we got a value from Redis, refresh local cache
            if (valueWrapper != null && localCache.size() < MAX_LOCAL_CACHE_SIZE) {
                localCache.put(key, valueWrapper.get());
            }

            if (valueWrapper == null) {
                // Cache miss
                monitor.recordMiss(getNormalizedKey(key));
            } else {
                // Cache hit
                monitor.recordHit(getNormalizedKey(key));
            }

            return valueWrapper;
        } catch (Exception e) {
            LOGGER.warn("Failed to get key {} from Redis cache: {}", key, e.getMessage());

            // Try local cache as last resort on unexpected errors
            Object value = localCache.get(key);
            return value != null ? new SimpleValueWrapper(value) : null;
        }
    }

    @Override
    public void evict(Object key) {
        try {
            // Use circuit breaker for Redis eviction
            circuitBreaker.execute(() -> {
                super.evict(key);
                return null;
            });

            // Always remove from local cache
            localCache.remove(key);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Evicted key {} from cache", key);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to evict key {} from Redis cache: {}", key, e.getMessage());
            // Still remove from local cache
            localCache.remove(key);
        }
    }

    @Override
    public void clear() {
        try {
            // Use circuit breaker for Redis clear
            circuitBreaker.execute(() -> {
                super.clear();
                return null;
            });

            // Always clear local cache
            localCache.clear();

            LOGGER.info("Cache cleared");
        } catch (Exception e) {
            LOGGER.warn("Failed to clear Redis cache: {}", e.getMessage());
            // Still clear local cache
            localCache.clear();
        }
    }

    /**
     * Record metrics for cache operations
     */
    private void recordMetrics(Object key, String operation) {
        String keyString = getNormalizedKey(key);

        if ("access".equals(operation)) {
            monitor.recordAccess(keyString);
        } else if ("update".equals(operation)) {
            monitor.recordUpdate(keyString);
        }
    }

    /**
     * Normalize key format for metrics
     */
    private String getNormalizedKey(Object key) {
        if (key instanceof SimpleKey simpleKey) {
            return simpleKey.toString();
        } else {
            return key.toString();
        }
    }
}
