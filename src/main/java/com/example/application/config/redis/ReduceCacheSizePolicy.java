package com.example.application.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReduceCacheSizePolicy implements CachePolicy {

    private static final double REDUCTION_PERCENTAGE = 0.5; // Remove 50% of cache
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(30); // Default expiration time
    private static final Logger log = LoggerFactory.getLogger(ReduceCacheSizePolicy.class);

    @Override
    public Duration getExpirationTime() {
        return EXPIRATION_TIME;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys == null || allKeys.isEmpty()) {
            log.info("Cache is empty, nothing to reduce.");
            return;
        }

        int totalKeys = allKeys.size();
        int keysToRemove = (int) (totalKeys * REDUCTION_PERCENTAGE);

        log.info("Reducing cache size by removing {} entries.", keysToRemove);

        int removedCount = 0;
        for (String key : allKeys) {
            if (removedCount >= keysToRemove) break;
            redisTemplate.delete(key);
            removedCount++;
        }
        log.info("Cache reduction complete. Removed {} entries.", removedCount);
    }
}
