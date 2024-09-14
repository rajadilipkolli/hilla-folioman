package com.example.application.config.redis;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

public class CustomRedisCache extends RedisCache {

    private static final Logger log = LoggerFactory.getLogger(CustomRedisCache.class);
    private final Monitor monitor;

    public CustomRedisCache(
            String name,
            RedisCacheWriter cacheWriter,
            RedisSerializer<Object> valueSerializer,
            Duration ttl,
            Monitor monitor) {
        super(name, cacheWriter, RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl));
        this.monitor = monitor;
    }

    @Override
    public void put(Object key, Object value) {
        // Call the original implementation first
        super.put(key, value);

        // Custom logic after the put operation
        log.info("Put operation completed for key: {}, value: {}", key, value);

        // Additional custom steps can be added here
        // For example, send a notification, update logs, etc.
        monitor.recordUpdate(((SimpleKey) key).toString());
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        Cache.ValueWrapper valueWrapper = super.get(key);

        // Custom behavior after a get operation
        log.info("Retrieved from cache for key: {}", key);
        monitor.recordAccess(((SimpleKey) key).toString());
        return valueWrapper;
    }
}