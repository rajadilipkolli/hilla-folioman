package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdjustTTLPolicy implements CachePolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdjustTTLPolicy.class);

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final Duration MAX_TTL = Duration.ofHours(2);
    private static final Duration MIN_TTL = Duration.ofMinutes(15);

    @Override
    public Duration getExpirationTime() {
        return DEFAULT_TTL;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {

        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys == null || allKeys.isEmpty()) {
            LOGGER.info("Cache is empty. No TTL adjustments needed.");
            return;
        }

        for (String key : allKeys) {
            double accessCount = getAccessCountForKey(meterRegistry, key);
            Duration newTTL = determineNewTTL(accessCount);
            redisTemplate.expire(key, newTTL);
            LOGGER.debug("Adjusted TTL for key: {} to {} minutes.", key, newTTL.toMinutes());
        }
    }

    private double getAccessCountForKey(MeterRegistry meterRegistry, String key) {
        if (key.indexOf("SimpleKey") > 0) {
            key = key.substring(key.indexOf("SimpleKey"));
        } else if (key.indexOf("::") > 0) {
            key = key.substring(key.indexOf("::") + 2);
        }
        return meterRegistry.counter("cache.access", "key", key).count();
    }

    private Duration determineNewTTL(double accessCount) {
        if (accessCount > 50) {
            return MAX_TTL;
        } else if (accessCount < 10) {
            return MIN_TTL;
        } else {
            long minTTLSeconds = MIN_TTL.getSeconds();
            long maxTTLSeconds = MAX_TTL.getSeconds();
            long defaultTTLSeconds = DEFAULT_TTL.getSeconds();
            double ttlSeconds = minTTLSeconds + (accessCount - 10) * (maxTTLSeconds - minTTLSeconds) / 40;
            return Duration.ofSeconds((long) Math.min(ttlSeconds, defaultTTLSeconds));
        }
    }
}
