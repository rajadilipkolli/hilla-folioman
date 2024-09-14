package com.example.application.config.redis;

import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;

public interface CachePolicy {

    Duration getExpirationTime();

    void apply(RedisTemplate<String, Object> redisTemplate);
}
