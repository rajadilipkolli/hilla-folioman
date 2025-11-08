package com.app.folioman.config.redis;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

public class CustomRedisCacheManager extends RedisCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRedisCacheManager.class);
    private final RedisCacheWriter redisCacheWriter;
    private final RedisSerializer<Object> redisSerializer;
    private final Duration defaultTtl;
    private final Monitor monitor;
    private final CacheCircuitBreaker circuitBreaker;

    public CustomRedisCacheManager(
            RedisCacheWriter cacheWriter, Monitor monitor, CacheCircuitBreaker circuitBreaker, Duration defaultTtl) {
        super(cacheWriter, RedisCacheConfiguration.defaultCacheConfig());
        this.redisCacheWriter = cacheWriter;
        this.redisSerializer = RedisSerializer.java(); // Custom serializer if needed
        this.defaultTtl = defaultTtl != null ? defaultTtl : Duration.ofMinutes(10);
        this.monitor = monitor;
        this.circuitBreaker = circuitBreaker;

        LOGGER.info("Initializing custom Redis cache manager with default TTL: {}", this.defaultTtl);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        // Configure cache with either provided config or default
        Duration ttl =
                cacheConfig != null ? cacheConfig.getTtlFunction().getTimeToLive(Object.class, null) : defaultTtl;

        LOGGER.debug("Creating Redis cache '{}' with TTL: {}", name, ttl);

        // Return the custom RedisCache implementation with circuit breaker support
        return new CustomRedisCache(name, redisCacheWriter, redisSerializer, ttl, monitor, circuitBreaker);
    }
}
