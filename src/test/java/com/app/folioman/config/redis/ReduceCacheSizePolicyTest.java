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
import java.util.LinkedHashSet;
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
        // Use a LinkedHashSet to preserve insertion order so the sequence of
        // counter.count() invocations maps deterministically to keys.
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.add("critical:important");
        keys.add("regular:key1");
        keys.add("regular:key2");
        keys.add("regular:key3");
        keys.add("regular:key4"); // will be the lowest-access key in this test

        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            // For 5 keys, 30% -> floor(1.5) == 1 keyToRemove. Provide counts in
            // the same order as the keys so that the lowest-access key is
            // predictable (regular:key4).
            when(counter.count()).thenReturn(100.0, 50.0, 40.0, 1.0, 0.5);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            // Ensure the high-access protected key is never deleted
            verify(redisTemplate, never()).delete("critical:important");

            // Exactly one deletion should occur for the lowest-access key
            verify(redisTemplate, times(1)).delete("regular:key4");
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
        // Use a deterministic collection so we can map counts and TTLs to keys reliably
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.add("key1");
        keys.add("key2");
        keys.add("key3");
        keys.add("key4");

        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys("*")).thenReturn(keys);
        })) {
            // For 4 keys, 30% -> floor(1.2) == 1 keyToRemove. Provide counts in
            // the same order as the keys so that the lowest-access key is
            // predictable (key4). Make key1 and key2 low-access (<5) so they
            // will receive TTL adjustments after eviction of key4.
            when(counter.count()).thenReturn(1.0, 0.5, 50.0, 0.1);

            // Mock TTLs per key: key1 has negative TTL, key2 has null TTL, key3/key4 have normal TTLs
            when(redisTemplate.getExpire("key1")).thenReturn(-1L);
            when(redisTemplate.getExpire("key2")).thenReturn(null);
            when(redisTemplate.getExpire("key3")).thenReturn(3600L);
            when(redisTemplate.getExpire("key4")).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            // Exactly one deletion should occur for a low-access key
            verify(redisTemplate, times(1)).delete(anyString());

            // Ensure keys with negative/null TTLs are adjusted and not deleted
            verify(redisTemplate, never()).delete("key1");
            verify(redisTemplate, never()).delete("key2");

            // TTL adjustments should be applied for key1 (negative) and key2 (null)
            verify(redisTemplate).expire(eq("key1"), eq(Duration.ofMinutes(15)));
            verify(redisTemplate).expire(eq("key2"), eq(Duration.ofMinutes(15)));
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
