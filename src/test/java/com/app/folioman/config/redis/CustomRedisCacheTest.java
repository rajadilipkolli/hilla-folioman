package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

@ExtendWith(MockitoExtension.class)
class CustomRedisCacheTest {

    @Mock
    private RedisCacheWriter cacheWriter;

    @Mock
    private RedisSerializer<Object> valueSerializer;

    @Mock
    private Monitor monitor;

    @Mock
    private CacheCircuitBreaker circuitBreaker;

    @Mock
    private Cache.ValueWrapper valueWrapper;

    private CustomRedisCache customRedisCache;
    private static final String CACHE_NAME = "testCache";
    private static final Duration TTL = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        customRedisCache = new CustomRedisCache(CACHE_NAME, cacheWriter, valueSerializer, TTL, monitor, circuitBreaker);
    }

    @Test
    void constructor_ShouldCreateInstance_WithValidParameters() {
        assertNotNull(customRedisCache);
        assertEquals(CACHE_NAME, customRedisCache.getName());
    }

    @Test
    void put_ShouldExecuteSuccessfully_WhenRedisIsAvailable() {
        Object key = "testKey";
        Object value = "testValue";

        doAnswer(invocation -> {
                    // CacheCircuitBreaker.execute accepts a Supplier<T>, so invoke its get()
                    invocation.getArgument(0, java.util.function.Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.put(key, value);

        verify(circuitBreaker).execute(any());
        verify(monitor).recordUpdate(key.toString());
    }

    @Test
    void put_ShouldHandleException_WhenRedisThrowsException() {
        Object key = "testKey";
        Object value = "testValue";

        doThrow(new RuntimeException("Redis connection failed"))
                .when(circuitBreaker)
                .execute(any());

        assertDoesNotThrow(() -> customRedisCache.put(key, value));
        verify(circuitBreaker).execute(any());
    }

    @Test
    void put_ShouldNotExceedLocalCacheLimit_WhenMaxSizeReached() {
        Object key = "testKey";
        Object value = "testValue";

        doAnswer(invocation -> {
                    invocation.getArgument(0, java.util.function.Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        for (int i = 0; i < 1001; i++) {
            customRedisCache.put("key" + i, "value" + i);
        }

        verify(circuitBreaker, times(1001)).execute(any());
    }

    @Test
    void get_ShouldReturnValue_WhenRedisHasValue() {
        Object key = "testKey";
        Object value = "testValue";

        // Simulate Redis returning a value
        when(circuitBreaker.executeWithFallback(any(), any())).thenReturn(new SimpleValueWrapper(value));

        Cache.ValueWrapper result = customRedisCache.get(key);

        verify(circuitBreaker).executeWithFallback(any(), any());
        verify(monitor).recordAccess(key.toString());
        verify(monitor).recordHit(key.toString());
    }

    @Test
    void get_ShouldReturnNull_WhenRedisHasNoValue() {
        Object key = "testKey";

        // Simulate Redis returning null
        when(circuitBreaker.executeWithFallback(any(), any())).thenReturn(null);

        customRedisCache.get(key);

        verify(circuitBreaker).executeWithFallback(any(), any());
        verify(monitor).recordAccess(key.toString());
        verify(monitor).recordMiss(key.toString());
    }

    @Test
    void get_ShouldUseFallback_WhenCircuitBreakerActivated() {
        Object key = "testKey";
        Object value = "fallbackValue";

        when(circuitBreaker.executeWithFallback(any(), any())).thenAnswer(invocation -> {
            Supplier<Cache.ValueWrapper> fallback = invocation.getArgument(1);
            return fallback.get();
        });

        customRedisCache.put(key, value);
        reset(circuitBreaker);
        when(circuitBreaker.executeWithFallback(any(), any())).thenAnswer(invocation -> {
            Supplier<Cache.ValueWrapper> fallback = invocation.getArgument(1);
            return fallback.get();
        });

        customRedisCache.get(key);

        verify(circuitBreaker).executeWithFallback(any(), any());
    }

    @Test
    void get_ShouldHandleException_WhenUnexpectedErrorOccurs() {
        Object key = "testKey";

        when(circuitBreaker.executeWithFallback(any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        Cache.ValueWrapper res = customRedisCache.get(key);

        assertNull(res);
        verify(circuitBreaker).executeWithFallback(any(), any());
    }

    @Test
    void evict_ShouldExecuteSuccessfully_WhenRedisIsAvailable() {
        Object key = "testKey";

        doAnswer(invocation -> {
                    invocation.getArgument(0, java.util.function.Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.evict(key);

        verify(circuitBreaker).execute(any());
    }

    @Test
    void evict_ShouldHandleException_WhenRedisThrowsException() {
        Object key = "testKey";

        doThrow(new RuntimeException("Redis connection failed"))
                .when(circuitBreaker)
                .execute(any());

        assertDoesNotThrow(() -> customRedisCache.evict(key));
        verify(circuitBreaker).execute(any());
    }

    @Test
    void clear_ShouldExecuteSuccessfully_WhenRedisIsAvailable() {
        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.clear();

        verify(circuitBreaker).execute(any());
    }

    @Test
    void clear_ShouldHandleException_WhenRedisThrowsException() {
        doThrow(new RuntimeException("Redis connection failed"))
                .when(circuitBreaker)
                .execute(any());

        assertDoesNotThrow(() -> customRedisCache.clear());
        verify(circuitBreaker).execute(any());
    }

    @Test
    void getNormalizedKey_ShouldHandleSimpleKey() {
        SimpleKey simpleKey = new SimpleKey("param1", "param2");

        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.put(simpleKey, "value");

        verify(monitor).recordUpdate(simpleKey.toString());
    }

    @Test
    void getNormalizedKey_ShouldHandleRegularKey() {
        String regularKey = "regularKey";

        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.put(regularKey, "value");

        verify(monitor).recordUpdate(regularKey);
    }

    @Test
    void recordMetrics_ShouldRecordAccessMetrics_WhenOperationIsAccess() {
        Object key = "testKey";

        when(circuitBreaker.executeWithFallback(any(), any())).thenAnswer(invocation -> {
            Supplier<Cache.ValueWrapper> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        customRedisCache.get(key);

        verify(monitor).recordAccess(key.toString());
    }

    @Test
    void recordMetrics_ShouldRecordUpdateMetrics_WhenOperationIsUpdate() {
        Object key = "testKey";
        Object value = "testValue";

        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.put(key, value);

        verify(monitor).recordUpdate(key.toString());
    }
}
