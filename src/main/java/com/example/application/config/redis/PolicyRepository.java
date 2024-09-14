package com.example.application.config.redis;

import org.springframework.stereotype.Component;

@Component
public class PolicyRepository {

    public CachePolicy getPolicy(String strategy) {
        return switch (strategy) {
            case "REDUCE_CACHE_SIZE" -> new ReduceCacheSizePolicy();
            case "INCREASE_CACHE_SIZE" -> new IncreaseCacheSizePolicy();
            case "ADJUST_TTL" -> new AdjustTTLPolicy();
            default -> new DefaultPolicy();
        };
    }
}
