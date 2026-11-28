package com.app.folioman.config.redis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Component
public class Monitor {

    static final String CACHE_KEY_PREFIX = "folioman:cache:";
    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);
    private static final int SCAN_COUNT = 100;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    private final @Nullable CacheCircuitBreaker cacheCircuitBreaker;

    public Monitor(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this(redisTemplate, meterRegistry, null);
    }

    @Autowired
    public Monitor(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            @Nullable CacheCircuitBreaker cacheCircuitBreaker) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        this.cacheCircuitBreaker = cacheCircuitBreaker;
    }

    public void recordAccess(String cacheName) {
        meterRegistry.counter("cache.access", "cache", cacheName).increment();
    }

    public void recordUpdate(String cacheName) {
        meterRegistry.counter("cache.update", "cache", cacheName).increment();
    }

    public void recordHit(String cacheName) {
        meterRegistry.counter("cache.hit", "cache", cacheName).increment();
    }

    public void recordMiss(String cacheName) {
        meterRegistry.counter("cache.miss", "cache", cacheName).increment();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", countKeys());
        metrics.put("hitRate", calculateHitRate());
        metrics.put("memoryUsage", getRedisMemoryUsage());

        String circuitBreakerState = getCircuitBreakerState();
        metrics.put("localCacheEnabled", false);
        metrics.put("circuitBreakerState", circuitBreakerState);

        return metrics;
    }

    private String getCircuitBreakerState() {
        if (cacheCircuitBreaker != null) {
            return cacheCircuitBreaker.getState().name();
        }
        return "CLOSED";
    }

    private long countKeys() {
        try {
            return scanKeys(CACHE_KEY_PREFIX + "*").size();
        } catch (Exception e) {
            LOGGER.error("Error counting Redis keys", e);
            return 0;
        }
    }

    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();

        if (factory == null) {
            return keys;
        }

        try {
            redisTemplate.execute((RedisConnection connection) -> {
                try (Cursor<byte[]> cursor = connection
                        .keyCommands()
                        .scan(ScanOptions.scanOptions()
                                .match(pattern)
                                .count(SCAN_COUNT)
                                .build())) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next()));
                    }
                } catch (Exception e) {
                    LOGGER.error("Error scanning Redis keys", e);
                }
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("Error executing Redis scan operation", e);
        }

        return keys;
    }

    /**
     * Computes hit rate from pod-local Micrometer counters (`cache.hit` / `cache.miss`).
     * In multi-pod deployments this reflects only the current pod's traffic.
     */
    private double calculateHitRate() {
        double hits = meterRegistry.find("cache.hit").counters().stream()
                .mapToDouble(Counter::count)
                .sum();
        double misses = meterRegistry.find("cache.miss").counters().stream()
                .mapToDouble(Counter::count)
                .sum();
        double totalRequests = hits + misses;

        if (totalRequests == 0) {
            return 0.0;
        }

        return hits / totalRequests;
    }

    private long getRedisMemoryUsage() {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory == null) {
            return 0L;
        }

        try (RedisConnection connection = factory.getConnection()) {
            Properties memoryInfo = connection.serverCommands().info("memory");
            String usedMemory = (String) memoryInfo.get("used_memory");

            return usedMemory != null ? Long.parseLong(usedMemory) : 0L;
        } catch (Exception e) {
            LOGGER.warn("Error getting Redis memory usage", e);
            return 0L;
        }
    }
}
