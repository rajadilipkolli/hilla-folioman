package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DefaultPolicy implements CachePolicy {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    private static final Logger log = LoggerFactory.getLogger(DefaultPolicy.class);

    @Override
    public Duration getExpirationTime() {
        return DEFAULT_TTL;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        log.info("Applying default cache policy. No action taken.");
    }
}
