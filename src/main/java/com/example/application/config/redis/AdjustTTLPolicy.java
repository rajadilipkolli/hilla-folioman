package com.example.application.config.redis;

import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdjustTTLPolicy implements CachePolicy {

    private static final Logger log = LoggerFactory.getLogger(AdjustTTLPolicy.class);

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final Duration MAX_TTL = Duration.ofHours(2);
    private static final Duration MIN_TTL = Duration.ofMinutes(5);
    private static final String ACCESS_COUNT_HASH_KEY = "access_counts";

    @Override
    public Duration getExpirationTime() {
        return DEFAULT_TTL;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate) {

        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys == null || allKeys.isEmpty()) {
            log.info("Cache is empty. No TTL adjustments needed.");
            return;
        }

        for (String key : allKeys) {
            long accessCount = getAccessCountForKey(redisTemplate, key);
            Duration newTTL = determineNewTTL(accessCount);
            redisTemplate.expire(key, newTTL);
            log.info("Adjusted TTL for key: {} to {} minutes.", key, newTTL.toMinutes());
        }
    }

    private long getAccessCountForKey(RedisTemplate<String, Object> redisTemplate, String key) {
        Long count = (Long) redisTemplate.opsForHash().get(ACCESS_COUNT_HASH_KEY, key);
        return count != null ? count : 0;
    }

    private Duration determineNewTTL(long accessCount) {
        if (accessCount > 50) {
            return MAX_TTL;
        } else if (accessCount < 10) {
            return MIN_TTL;
        } else {
            long minTTLSeconds = MIN_TTL.getSeconds();
            long maxTTLSeconds = MAX_TTL.getSeconds();
            long defaultTTLSeconds = DEFAULT_TTL.getSeconds();
            long ttlSeconds = minTTLSeconds + (accessCount - 10) * (maxTTLSeconds - minTTLSeconds) / 40;
            return Duration.ofSeconds(Math.min(ttlSeconds, defaultTTLSeconds));
        }
    }
}
