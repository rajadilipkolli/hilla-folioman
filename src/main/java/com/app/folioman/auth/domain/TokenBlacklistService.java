package com.app.folioman.auth.domain;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(@Nullable String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        try {
            redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(ttlMillis));
            LOGGER.debug("Blacklisted token with jti: {} for {} ms", jti, ttlMillis);
        } catch (Exception e) {
            LOGGER.warn("Failed to blacklist token with jti: {}. Redis might be down.", jti, e);
        }
    }

    public boolean isBlacklisted(@Nullable String jti) {
        if (jti == null) {
            return false;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            LOGGER.warn("Failed to check token blacklist for jti: {}. Assuming not blacklisted.", jti, e);
            // Fail closed to preserve revocation guarantees when Redis is unavailable
            return true;
        }
    }
}
