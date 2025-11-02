package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluatorTest {

    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new Evaluator();
    }

    @Test
    void evaluate_shouldReturnReduceCacheSize_whenLowHitRateAndLargeCacheSize() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 15000L);
        metrics.put("hitRate", 0.2);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("REDUCE_CACHE_SIZE", result);
    }

    @Test
    void evaluate_shouldReturnIncreaseCacheSize_whenHighHitRateAndLowMemoryUsage() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.9);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("INCREASE_CACHE_SIZE", result);
    }

    @Test
    void evaluate_shouldReturnAdjustTtl_whenLowHitRateButSmallCacheSize() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.4);
        metrics.put("memoryUsage", 2000000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("ADJUST_TTL", result);
    }

    @Test
    void evaluate_shouldReturnMaintainCurrent_whenNoConditionsMet() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.6);
        metrics.put("memoryUsage", 2000000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldReturnMaintainCurrent_whenHitRateAtBoundaryValue() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.5);
        metrics.put("memoryUsage", 2000000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldReturnReduceCacheSize_whenHitRateAtLowThreshold() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 15000L);
        metrics.put("hitRate", 0.3);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldReturnIncreaseCacheSize_whenHitRateAtHighThreshold() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.8);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldReturnIncreaseCacheSize_whenMemoryUsageAtThreshold() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.9);
        metrics.put("memoryUsage", 1000000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldThrowException_whenCacheSizeKeyMissing() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("hitRate", 0.5);
        metrics.put("memoryUsage", 500000000L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Input map does not contain the expected keys", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenHitRateKeyMissing() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("memoryUsage", 500000000L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Input map does not contain the expected keys", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenMemoryUsageKeyMissing() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.5);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Input map does not contain the expected keys", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenCacheSizeIsNegative() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", -1L);
        metrics.put("hitRate", 0.5);
        metrics.put("memoryUsage", 500000000L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Invalid metric values", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenHitRateIsNegative() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", -0.1);
        metrics.put("memoryUsage", 500000000L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Invalid metric values", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenHitRateIsGreaterThanOne() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 1.1);
        metrics.put("memoryUsage", 500000000L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Invalid metric values", exception.getMessage());
    }

    @Test
    void evaluate_shouldThrowException_whenMemoryUsageIsNegative() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.5);
        metrics.put("memoryUsage", -1L);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(metrics));

        assertEquals("Invalid metric values", exception.getMessage());
    }

    @Test
    void evaluate_shouldSucceed_whenHitRateIsZero() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.0);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("ADJUST_TTL", result);
    }

    @Test
    void evaluate_shouldSucceed_whenHitRateIsOne() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 1.0);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("INCREASE_CACHE_SIZE", result);
    }

    @Test
    void evaluate_shouldSucceed_whenCacheSizeIsZero() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 0L);
        metrics.put("hitRate", 0.5);
        metrics.put("memoryUsage", 500000000L);

        String result = evaluator.evaluate(metrics);

        assertEquals("MAINTAIN_CURRENT", result);
    }

    @Test
    void evaluate_shouldSucceed_whenMemoryUsageIsZero() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheSize", 5000L);
        metrics.put("hitRate", 0.9);
        metrics.put("memoryUsage", 0L);

        String result = evaluator.evaluate(metrics);

        assertEquals("INCREASE_CACHE_SIZE", result);
    }
}
