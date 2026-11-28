package com.app.folioman.config.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class CustomRedisCacheManager extends RedisCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRedisCacheManager.class);
    private final RedisCacheWriter redisCacheWriter;
    private final RedisCacheConfiguration defaultCacheConfiguration;
    private final Duration defaultTtl;
    private final Monitor monitor;
    private final CacheCircuitBreaker circuitBreaker;

    public CustomRedisCacheManager(
            RedisCacheWriter cacheWriter,
            Map<String, RedisCacheConfiguration> initialCacheConfigurations,
            Monitor monitor,
            CacheCircuitBreaker circuitBreaker,
            Duration defaultTtl) {
        this(
                cacheWriter,
                RedisCacheConfiguration.defaultCacheConfig(),
                initialCacheConfigurations,
                monitor,
                circuitBreaker,
                defaultTtl);
    }

    public CustomRedisCacheManager(
            RedisCacheWriter cacheWriter,
            RedisCacheConfiguration defaultCacheConfiguration,
            Map<String, RedisCacheConfiguration> initialCacheConfigurations,
            Monitor monitor,
            CacheCircuitBreaker circuitBreaker,
            Duration defaultTtl) {
        super(
                cacheWriter,
                defaultCacheConfiguration,
                initialCacheConfigurations != null ? initialCacheConfigurations : Collections.emptyMap());
        this.redisCacheWriter = cacheWriter;
        this.defaultCacheConfiguration = defaultCacheConfiguration;
        this.defaultTtl = defaultTtl != null ? defaultTtl : Duration.ofMinutes(10);
        this.monitor = monitor;
        this.circuitBreaker = circuitBreaker;

        LOGGER.info("Initializing custom Redis cache manager with default TTL: {}", this.defaultTtl);
    }

    @Override
    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
        RedisCacheConfiguration effectiveConfig =
                cacheConfig != null ? cacheConfig : defaultCacheConfiguration.entryTtl(defaultTtl);

        LOGGER.debug(
                "Creating Redis cache '{}' with TTL: {}",
                name,
                effectiveConfig.getTtlFunction().getTimeToLive(Object.class, null));

        return new CustomRedisCache(name, redisCacheWriter, effectiveConfig, monitor, circuitBreaker);
    }
}
