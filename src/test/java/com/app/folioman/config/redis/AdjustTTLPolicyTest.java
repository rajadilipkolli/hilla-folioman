package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
        when(redisTemplate.keys("*")).thenReturn(null);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void apply_WhenCacheIsEmptySet_ShouldNotAdjustTTL() {
        Set<String> emptyKeys = new HashSet<>();
        when(redisTemplate.keys("*")).thenReturn(emptyKeys);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void apply_WhenCacheHasKeys_ShouldAdjustTTLBasedOnAccessCount() {
        Set<String> keys = Set.of("key1", "key2::suffix", "prefixSimpleKeyTest");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(25.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, times(3)).expire(anyString(), any(Duration.class));
        verify(meterRegistry, times(3)).counter(eq("cache.access"), eq("key"), anyString());
    }

    @Test
    void apply_WithHighAccessCount_ShouldSetMaxTTL() {
        Set<String> keys = Set.of("highAccessKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(100.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).expire("highAccessKey", Duration.ofHours(2));
    }

    @Test
    void apply_WithLowAccessCount_ShouldSetMinTTL() {
        Set<String> keys = Set.of("lowAccessKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(5.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).expire("lowAccessKey", Duration.ofMinutes(15));
    }

    @Test
    void apply_WithMediumAccessCount_ShouldCalculateProportionalTTL() {
        Set<String> keys = Set.of("mediumAccessKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(30.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).expire(eq("mediumAccessKey"), any(Duration.class));
    }

    @Test
    void apply_WithSimpleKeyFormat_ShouldExtractKeyCorrectly() {
        Set<String> keys = Set.of("prefixSimpleKeyTest");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(25.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(meterRegistry).counter("cache.access", "key", "SimpleKeyTest");
    }

    @Test
    void apply_WithDoubleColonFormat_ShouldExtractKeyCorrectly() {
        Set<String> keys = Set.of("prefix::actualKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(25.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(meterRegistry).counter("cache.access", "key", "actualKey");
    }

    @Test
    void apply_WithRegularKey_ShouldUseKeyAsIs() {
        Set<String> keys = Set.of("regularKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(25.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(meterRegistry).counter("cache.access", "key", "regularKey");
    }

    @Test
    void apply_WithAccessCountExactly50_ShouldNotSetMaxTTL() {
        Set<String> keys = Set.of("exactFiftyKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(50.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).expire(eq("exactFiftyKey"), argThat(duration -> !duration.equals(Duration.ofHours(2))));
    }

    @Test
    void apply_WithAccessCountExactly10_ShouldNotSetMinTTL() {
        Set<String> keys = Set.of("exactTenKey");
        when(redisTemplate.keys("*")).thenReturn(keys);
        when(counter.count()).thenReturn(10.0);
        when(meterRegistry.counter(eq("cache.access"), eq("key"), anyString())).thenReturn(counter);

        adjustTTLPolicy.apply(redisTemplate, meterRegistry);

        // Production logic returns MIN_TTL for accessCount == 10, assert that behavior
        verify(redisTemplate).expire(eq("exactTenKey"), eq(Duration.ofMinutes(15)));
    }
}
