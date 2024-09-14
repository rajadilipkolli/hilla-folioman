package com.example.application.config.redis;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class Evaluator {

    public String evaluate(Map<String, Object> metrics) {
        long cacheSize = (int) metrics.get("cacheSize");
        double hitRate = (double) metrics.get("hitRate");
        long memoryUsage = (long) metrics.get("memoryUsage");

        if (hitRate < 0.3 && cacheSize > 10000) {
            return "REDUCE_CACHE_SIZE";
        } else if (hitRate > 0.8 && memoryUsage < 1000000000) {
            return "INCREASE_CACHE_SIZE";
        } else if (hitRate < 0.5) {
            return "ADJUST_TTL";
        }
        return "MAINTAIN_CURRENT";
    }
}
