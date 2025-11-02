package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
import org.mockito.MockedStatic;
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
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);
    }

    @Test
    void getExpirationTime_ReturnsThirtyMinutes() {
        Duration result = reduceCacheSizePolicy.getExpirationTime();
        assertEquals(Duration.ofMinutes(30), result);
    }

    @Test
    void apply_WithEmptyKeys_LogsAndReturns() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);
            when(monitor.scanKeys("*")).thenReturn(new HashSet<>());

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(monitor).scanKeys("*");
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Test
    void apply_WithNullKeys_LogsAndReturns() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);
            when(monitor.scanKeys("*")).thenReturn(null);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(monitor).scanKeys("*");
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Test
    void apply_WithRegularKeys_EvictsKeysAndAppliesTTL() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("key1", "key2", "key3", "key4", "key5");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(2.0, 1.0, 3.0, 0.5, 4.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(1)).delete(anyString());
            verify(redisTemplate).expire(anyString(), eq(Duration.ofMinutes(15)));
        }
    }

    @Test
    void apply_WithProtectedKeys_SkipsProtectedKeys() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("critical:important", "regular:key1", "regular:key2");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(1.0, 2.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, never()).delete("critical:important");
        }
    }

    @Test
    void apply_WithHighAccessKeys_DoesNotEvictHighAccessKeys() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("key1", "key2", "key3");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(10.0, 15.0, 1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(1)).delete("key3");
            verify(redisTemplate, never()).delete("key1");
            verify(redisTemplate, never()).delete("key2");
        }
    }

    @Test
    void apply_WithNegativeTTL_HandlesNegativeTTL() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("key1", "key2");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(1.0, 2.0);
            when(redisTemplate.getExpire("key1")).thenReturn(-1L);
            when(redisTemplate.getExpire("key2")).thenReturn(null);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate).delete(anyString());
        }
    }

    @Test
    void apply_WithSimpleKeyInKeyName_ProcessesKeyCorrectly() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("cache::SimpleKey[param1]");
            when(monitor.scanKeys("*")).thenReturn(keys);
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
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("cache::methodName");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(meterRegistry.counter("cache.access", "key", "methodName")).thenReturn(counter);
            when(counter.count()).thenReturn(1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(meterRegistry).counter("cache.access", "key", "methodName");
        }
    }

    @Test
    void apply_WithLowAccessCountKeys_AppliesShortTTL() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = Set.of("key1", "key2", "key3", "key4", "key5", "key6");
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(1.0, 2.0, 3.0, 4.0, 6.0, 7.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(1)).delete(anyString());
            verify(redisTemplate).expire(anyString(), eq(Duration.ofMinutes(15)));
        }
    }

    @Test
    void apply_WithManyKeysToRemove_ProcessesInBatches() {
        try (MockedStatic<Monitor> monitorMock = mockStatic(Monitor.class)) {
            Monitor monitor = mock(Monitor.class);
            monitorMock.when(() -> new Monitor(redisTemplate, meterRegistry)).thenReturn(monitor);

            Set<String> keys = new HashSet<>();
            for (int i = 0; i < 500; i++) {
                keys.add("key" + i);
            }
            when(monitor.scanKeys("*")).thenReturn(keys);
            when(counter.count()).thenReturn(1.0);
            when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

            reduceCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(redisTemplate, times(150)).delete(anyString());
        }
    }
}
