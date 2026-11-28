package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
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

    private CustomRedisCache customRedisCache;
    private static final String CACHE_NAME = "testCache";
    private static final Duration TTL = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        customRedisCache = new CustomRedisCache(CACHE_NAME, cacheWriter, valueSerializer, TTL, monitor, circuitBreaker);
    }

    @Test
    void constructor_ShouldCreateInstance_WithValidParameters() {
        assertThat(customRedisCache).isNotNull();
        assertThat(customRedisCache.getName()).isEqualTo(CACHE_NAME);
    }

    @Test
    void put_ShouldExecuteSuccessfully_WhenRedisIsAvailable() {
        Object key = "testKey";
        Object value = "testValue";

        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
                    return null;
                })
                .when(circuitBreaker)
                .execute(any());

        customRedisCache.put(key, value);

        verify(circuitBreaker).execute(any());
        verify(monitor).recordUpdate(CACHE_NAME);
    }

    @Test
    void put_ShouldHandleException_WhenRedisThrowsException() {
        Object key = "testKey";
        Object value = "testValue";

        doThrow(new RuntimeException("Redis connection failed"))
                .when(circuitBreaker)
                .execute(any());

        assertThatCode(() -> customRedisCache.put(key, value)).doesNotThrowAnyException();
        verify(circuitBreaker).execute(any());
    }

    @Test
    void get_ShouldReturnValue_WhenRedisHasValue() {
        Object key = "testKey";
        Object value = "testValue";

        when(circuitBreaker.executeWithFallback(any(), any())).thenReturn(new SimpleValueWrapper(value));

        Cache.ValueWrapper result = customRedisCache.get(key);

        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo(value);
        verify(circuitBreaker).executeWithFallback(any(), any());
        verify(monitor).recordAccess(CACHE_NAME);
        verify(monitor).recordHit(CACHE_NAME);
    }

    @Test
    void get_ShouldReturnNull_WhenRedisHasNoValue() {
        Object key = "testKey";

        when(circuitBreaker.executeWithFallback(any(), any())).thenReturn(null);

        Cache.ValueWrapper result = customRedisCache.get(key);

        assertThat(result).isNull();
        verify(monitor).recordAccess(CACHE_NAME);
        verify(monitor).recordMiss(CACHE_NAME);
    }

    @Test
    void get_ShouldReturnNull_WhenCircuitBreakerFallsBack() {
        Object key = "testKey";

        when(circuitBreaker.executeWithFallback(any(), any())).thenAnswer(invocation -> {
            Supplier<?> fallback = invocation.getArgument(1, Supplier.class);
            return fallback.get();
        });

        Cache.ValueWrapper result = customRedisCache.get(key);

        assertThat(result).isNull();
        verify(monitor).recordAccess(CACHE_NAME);
        verify(monitor).recordMiss(CACHE_NAME);
    }

    @Test
    void get_ShouldHandleException_WhenUnexpectedErrorOccurs() {
        Object key = "testKey";

        when(circuitBreaker.executeWithFallback(any(), any())).thenThrow(new RuntimeException("Unexpected"));

        Cache.ValueWrapper result = customRedisCache.get(key);

        assertThat(result).isNull();
    }

    @Test
    void evict_ShouldExecuteSuccessfully_WhenRedisIsAvailable() {
        Object key = "testKey";

        doAnswer(invocation -> {
                    invocation.getArgument(0, Supplier.class).get();
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

        doThrow(new RuntimeException("Redis error")).when(circuitBreaker).execute(any());

        assertThatCode(() -> customRedisCache.evict(key)).doesNotThrowAnyException();
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
        doThrow(new RuntimeException("Redis error")).when(circuitBreaker).execute(any());

        assertThatCode(() -> customRedisCache.clear()).doesNotThrowAnyException();
    }
}
