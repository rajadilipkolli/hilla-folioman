package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IncreaseCacheSizePolicy implements CachePolicy {

    private static final Duration LONGER_EXPIRATION_TIME = Duration.ofHours(2);
    private static final Logger LOGGER = LoggerFactory.getLogger(IncreaseCacheSizePolicy.class);

    @Override
    public Duration getExpirationTime() {
        return LONGER_EXPIRATION_TIME;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        Monitor monitor = new Monitor(redisTemplate, meterRegistry);
        Set<String> allKeys = monitor.scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
        if (allKeys == null || allKeys.isEmpty()) {
            LOGGER.info("Cache is empty, no need to increase.");
            return;
        }

        LOGGER.info("Increasing TTL for {} cache entries.", allKeys.size());

        for (String key : allKeys) {
            redisTemplate.expire(key, LONGER_EXPIRATION_TIME);
            LOGGER.debug("Increased TTL for key: {}", key);
        }
    }
}
