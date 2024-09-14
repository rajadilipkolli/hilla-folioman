package com.example.application.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;

public interface CachePolicy {

    Duration getExpirationTime();

    void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry);
}
