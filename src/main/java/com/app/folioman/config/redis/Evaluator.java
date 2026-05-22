package com.app.folioman.config.redis;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
class Evaluator {
    private static final double LOW_HIT_RATE_THRESHOLD = 0.3;
    private static final double HIGH_HIT_RATE_THRESHOLD = 0.8;
    private static final int LARGE_CACHE_SIZE_THRESHOLD = 10000;
    private static final long LOW_MEMORY_USAGE_THRESHOLD = 1000000000L;

    /**
     * Evaluates cache performance based on metrics and returns a recommendation.
     *
     * @param metrics a map containing cache performance metrics
     * @return a string representing the recommended action to take based on the metrics
     * @throws IllegalArgumentException if the input map does not contain the expected keys or if the metric values are invalid
     */
    String evaluate(Map<String, Object> metrics) {
        long cacheSize = toLong(metrics.get("cacheSize"));
        double hitRate = toDouble(metrics.get("hitRate"));
        long memoryUsage = toLong(metrics.get("memoryUsage"));

        validateRanges(cacheSize, hitRate, memoryUsage);

        if (hitRate < LOW_HIT_RATE_THRESHOLD && cacheSize > LARGE_CACHE_SIZE_THRESHOLD) {
            return "REDUCE_CACHE_SIZE";
        } else if (hitRate > HIGH_HIT_RATE_THRESHOLD && memoryUsage < LOW_MEMORY_USAGE_THRESHOLD) {
            return "INCREASE_CACHE_SIZE";
        } else if (hitRate < 0.5) {
            return "ADJUST_TTL";
        }
        return "MAINTAIN_CURRENT";
    }

    private void validateRanges(long cacheSize, double hitRate, long memoryUsage) {
        if (cacheSize < 0 || hitRate < 0 || hitRate > 1 || memoryUsage < 0) {
            throw new IllegalArgumentException("Invalid metric values");
        }
    }

    private long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private double toDouble(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0.0;
    }
}
