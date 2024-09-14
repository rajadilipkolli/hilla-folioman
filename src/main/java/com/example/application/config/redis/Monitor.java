package com.example.application.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class Monitor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    @Autowired
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
     * Get key metrics including cache size, hit rate, and Redis memory usage.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", Objects.requireNonNull(redisTemplate.keys("*")).size());
        metrics.put("hitRate", calculateHitRate());
        metrics.put("memoryUsage", getRedisMemoryUsage());
        return metrics;
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
        }
    }
}
