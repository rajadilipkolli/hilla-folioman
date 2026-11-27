package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class AdjustTTLPolicyTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private AdjustTTLPolicy adjustTTLPolicy;

    @Test
    void getExpirationTime_ShouldReturnDefaultTTL() {
        Duration result = adjustTTLPolicy.getExpirationTime();
        assertThat(result).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void apply_WhenCacheIsEmpty_ShouldNotAdjustTTL() {
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(null);
        })) {
            adjustTTLPolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().getFirst()).scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
            verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        }
    }

    @Test
    void apply_WhenCacheIsEmptySet_ShouldNotAdjustTTL() {
        Set<String> emptyKeys = new HashSet<>();
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(emptyKeys);
        })) {
            adjustTTLPolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().getFirst()).scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
            verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        }
    }

    @Test
    void apply_WhenCacheHasKeys_ShouldAdjustTTLBasedOnAccessCount() {
        Set<String> keys = Set.of(
                "folioman:cache:schemeSearchCache::key1",
                "folioman:cache:summaryCache::key2",
                "folioman:cache:returnsCache::key3");
        try (MockedConstruction<Monitor> ignored = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(25.0);
            when(meterRegistry.counter(eq("cache.access"), eq("cache"), anyString()))
                    .thenReturn(counter);

            adjustTTLPolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(3)).expire(anyString(), any(Duration.class));
            verify(meterRegistry, times(3)).counter(eq("cache.access"), eq("cache"), anyString());
        }
    }

    @Test
    void apply_WithHighAccessCount_ShouldSetMaxTTL() {
        Set<String> keys = Set.of("folioman:cache:highAccessCache::k1");
        try (MockedConstruction<Monitor> ignored = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(100.0);
            when(meterRegistry.counter("cache.access", "cache", "highAccessCache"))
                    .thenReturn(counter);

            adjustTTLPolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate).expire("folioman:cache:highAccessCache::k1", Duration.ofHours(2));
        }
    }

    @Test
    void apply_WithLowAccessCount_ShouldSetMinTTL() {
        Set<String> keys = Set.of("folioman:cache:lowAccessCache::k1");
        try (MockedConstruction<Monitor> ignored = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(5.0);
            when(meterRegistry.counter("cache.access", "cache", "lowAccessCache"))
                    .thenReturn(counter);

            adjustTTLPolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate).expire("folioman:cache:lowAccessCache::k1", Duration.ofMinutes(15));
        }
    }
}
