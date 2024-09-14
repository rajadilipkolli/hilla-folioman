package com.example.application.config.redis;

import java.time.Duration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

public class CustomRedisCacheManager extends RedisCacheManager {

    private final RedisCacheWriter redisCacheWriter;
    private final RedisSerializer<Object> redisSerializer;
    private final Duration defaultTtl;
    private final Monitor monitor;

    public CustomRedisCacheManager(
            RedisCacheWriter cacheWriter, RedisConnectionFactory connectionFactory, Monitor monitor) {
        super(cacheWriter, RedisCacheConfiguration.defaultCacheConfig());
        this.redisCacheWriter = cacheWriter;
        this.redisSerializer = RedisSerializer.java(); // Custom serializer if needed
        this.defaultTtl = Duration.ofMinutes(10); // Default TTL for cache entries
        this.monitor = monitor;
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        // Return the custom RedisCache implementation
        return new CustomRedisCache(name, redisCacheWriter, redisSerializer, defaultTtl, monitor);
    }
}
