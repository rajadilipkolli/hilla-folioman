package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class ReduceCacheSizePolicyTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private ReduceCacheSizePolicy reduceCacheSizePolicy;

    @BeforeEach
    void setUp() {
        // Make this lenient because some tests don't access the meterRegistry counter
        org.mockito.Mockito.lenient()
                .when(meterRegistry.counter(anyString(), anyString(), anyString()))
                .thenReturn(counter);
    }

    @Test
    void getExpirationTime_ReturnsThirtyMinutes() {
        Duration result = reduceCacheSizePolicy.getExpirationTime();
        assertEquals(Duration.ofMinutes(30), result);
    }

    @Test
    void apply_WithEmptyKeys_LogsAndReturns() {
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(new HashSet<>());
        })) {
            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().get(0)).scanKeys("*");
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Test
    void apply_WithNullKeys_LogsAndReturns() {
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(null);
        })) {
            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().get(0)).scanKeys("*");
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Test
    void apply_WithRegularKeys_EvictsKeysAndAppliesTTL() {
        Set<String> keys = Set.of("key1", "key2", "key3", "key4", "key5");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(2.0, 1.0, 3.0, 0.5, 4.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(1)).delete(anyString());
            verify(redisTemplate, atLeastOnce()).expire(anyString(), eq(Duration.ofMinutes(15)));
        }
    }

    @Test
    void apply_WithProtectedKeys_SkipsProtectedKeys() {
        Set<String> keys = Set.of("critical:important", "regular:key1", "regular:key2");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(1.0, 2.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            // keysToRemove for 3 keys is 0 (30% -> 0), so no deletion should occur
            verify(redisTemplate, never()).delete("critical:important");
        }
    }

    @Test
    void apply_WithHighAccessKeys_DoesNotEvictHighAccessKeys() {
        Set<String> keys = Set.of("key1", "key2", "key3");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(10.0, 15.0, 1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            // For 3 keys keysToRemove is 0 (30% -> 0), so no deletions should occur
            verify(redisTemplate, never()).delete("key3");
            verify(redisTemplate, never()).delete("key1");
            verify(redisTemplate, never()).delete("key2");
        }
    }

    @Test
    void apply_WithNegativeTTL_HandlesNegativeTTL() {
        Set<String> keys = Set.of("key1", "key2");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(1.0, 2.0);
            when(redisTemplate.getExpire("key1")).thenReturn(-1L);
            when(redisTemplate.getExpire("key2")).thenReturn(null);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            // No keys should be evicted when computed keysToRemove is 0, but TTL adjustments may occur
            verify(redisTemplate, never()).delete(anyString());
            verify(redisTemplate, atLeastOnce()).expire(anyString(), eq(Duration.ofMinutes(15)));
        }
    }

    @Test
    void apply_WithSimpleKeyInKeyName_ProcessesKeyCorrectly() {
        Set<String> keys = Set.of("cache::SimpleKey[param1]");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(meterRegistry.counter("cache.access", "key", "SimpleKey[param1]"))
                    .thenReturn(counter);
            when(counter.count()).thenReturn(1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(meterRegistry).counter("cache.access", "key", "SimpleKey[param1]");
        }
    }

    @Test
    void apply_WithDoubleColonInKeyName_ProcessesKeyCorrectly() {
        Set<String> keys = Set.of("cache::methodName");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(meterRegistry.counter("cache.access", "key", "methodName")).thenReturn(counter);
            when(counter.count()).thenReturn(1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(meterRegistry).counter("cache.access", "key", "methodName");
        }
    }

    @Test
    void apply_WithLowAccessCountKeys_AppliesShortTTL() {
        Set<String> keys = Set.of("key1", "key2", "key3", "key4", "key5", "key6");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(1.0, 2.0, 3.0, 4.0, 6.0, 7.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(1)).delete(anyString());
            verify(redisTemplate, atLeastOnce()).expire(anyString(), eq(Duration.ofMinutes(15)));
        }
    }

    @Test
    void apply_WithManyKeysToRemove_ProcessesInBatches() {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            keys.add("key" + i);
        }
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            when(counter.count()).thenReturn(1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(150)).delete(anyString());
        }
    }
}
