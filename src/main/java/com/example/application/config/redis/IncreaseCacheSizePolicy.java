package com.example.application.config.redis;

import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IncreaseCacheSizePolicy implements CachePolicy {

    private static final Duration LONGER_EXPIRATION_TIME = Duration.ofHours(2); // Increased TTL
    private static final Logger log = LoggerFactory.getLogger(IncreaseCacheSizePolicy.class);

    @Override
    public Duration getExpirationTime() {
        return LONGER_EXPIRATION_TIME;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate) {
        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys == null || allKeys.isEmpty()) {
            log.info("Cache is empty, no need to increase.");
            return;
        }

        log.info("Increasing TTL for all cache entries.");

        for (String key : allKeys) {
            redisTemplate.expire(key, LONGER_EXPIRATION_TIME);
            log.info("Increased TTL for key: {}", key);
        }

        preloadFrequentlyUsedData(redisTemplate);
    }

    private void preloadFrequentlyUsedData(RedisTemplate<String, Object> redisTemplate) {
        String[] keysToPreload = {"item1", "item2", "item3"};
        for (String key : keysToPreload) {
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, "PreloadedValue_" + key, LONGER_EXPIRATION_TIME);
                log.info("Preloaded key: {} into cache.", key);
            }
        }
    }
}
