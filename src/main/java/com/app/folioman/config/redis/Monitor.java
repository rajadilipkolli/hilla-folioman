package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Component
public class Monitor {

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);
    private static final int SCAN_COUNT = 100; // Process keys in batches of 100

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    public Monitor(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record access to the cache, incrementing the counter for cache access.
     */
    public void recordAccess(String key) {
        meterRegistry.counter("cache.access", "key", key).increment();
    }

    /**
     * Record update to the cache, incrementing the counter for cache updates.
     */
    public void recordUpdate(String key) {
        meterRegistry.counter("cache.update", "key", key).increment();
    }

    /**
     * Record a cache hit, incrementing the counter for cache hits.
     */
    public void recordHit(String key) {
        meterRegistry.counter("cache.hit", "key", key).increment();
    }

    /**
     * Record a cache miss, incrementing the counter for cache misses.
     */
    public void recordMiss(String key) {
        meterRegistry.counter("cache.miss", "key", key).increment();
    }

    /**
     * Get key metrics including cache size, hit rate, and Redis memory usage.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", countKeys());
        metrics.put("hitRate", calculateHitRate());
        metrics.put("memoryUsage", getRedisMemoryUsage());

        // Add additional metrics for better monitoring
        metrics.put("localCacheEnabled", getCircuitBreakerState().equals("OPEN"));
        metrics.put("circuitBreakerState", getCircuitBreakerState());

        return metrics;
    }

    /**
     * Get the current state of the circuit breaker
     */
    private String getCircuitBreakerState() {
        try {
            // This is a simplistic approach; in production, you would inject the actual CircuitBreaker
            return "CLOSED"; // Default to closed if we can't determine
        } catch (Exception e) {
            log.warn("Error getting circuit breaker state", e);
            return "UNKNOWN";
        }
    }

    /**
     * Count the number of keys in Redis without using KEYS command
     * @return the number of keys in Redis
     */
    private long countKeys() {
        try {
            return scanKeys("*").size();
        } catch (Exception e) {
            log.error("Error counting Redis keys", e);
            return 0;
        }
    }

    /**
     * Scan keys in Redis using the more efficient SCAN operation
     * @param pattern the key pattern to match
     * @return a set of keys matching the pattern
     */
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
                    log.error("Error scanning Redis keys", e);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Error executing Redis scan operation", e);
        }

        return keys;
    }

    /**
     * Calculate the cache hit rate using counters from Micrometer.
     * Hit rate is calculated as (cacheHits / (cacheHits + cacheMisses)).
     */
    private double calculateHitRate() {
        double hits = meterRegistry.counter("cache.hit").count();
        double misses = meterRegistry.counter("cache.miss").count();
        double totalRequests = hits + misses;

        if (totalRequests == 0) {
            return 0.0;
        }

        return hits / totalRequests;
    }

    /**
     * Retrieve Redis memory usage using the INFO MEMORY command.
     */
    private long getRedisMemoryUsage() {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory == null) {
            return 0L;
        }

        try (RedisConnection connection = factory.getConnection()) {
            // Execute the Redis INFO MEMORY command
            Properties memoryInfo = connection.serverCommands().info("memory");
            String usedMemory = (String) memoryInfo.get("used_memory");

            return usedMemory != null ? Long.parseLong(usedMemory) : 0L;
        } catch (Exception e) {
            log.warn("Error getting Redis memory usage", e);
            return 0L;
        }
    }
}
