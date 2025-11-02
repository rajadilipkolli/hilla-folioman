package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncreaseCacheSizePolicyTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private IncreaseCacheSizePolicy increaseCacheSizePolicy;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getExpirationTime_ShouldReturnTwoHours() {
        Duration result = increaseCacheSizePolicy.getExpirationTime();
        assertEquals(Duration.ofHours(2), result);
    }

    @Test
    void apply_WhenCacheIsEmpty_ShouldLogInfoAndPreloadData() {
        when(redisTemplate.keys("*")).thenReturn(Collections.emptySet());
        when(redisTemplate.hasKey("item1")).thenReturn(false);
        when(redisTemplate.hasKey("item2")).thenReturn(false);
        when(redisTemplate.hasKey("item3")).thenReturn(false);

        increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        // Production returns early when cache is empty; no preloading occurs
        verify(valueOperations, never()).set(anyString(), anyString(), eq(Duration.ofHours(2)));
    }

    @Test
    void apply_WhenKeysIsNull_ShouldLogInfoAndPreloadData() {
        when(redisTemplate.keys("*")).thenReturn(null);
        when(redisTemplate.hasKey("item1")).thenReturn(false);
        when(redisTemplate.hasKey("item2")).thenReturn(false);
        when(redisTemplate.hasKey("item3")).thenReturn(false);

        increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        // Production returns early when keys() is null; no preloading occurs
        verify(valueOperations, never()).set(anyString(), anyString(), eq(Duration.ofHours(2)));
    }

    @Test
    void apply_WhenCacheHasKeys_ShouldIncreaseTtlAndPreloadData() {
        Set<String> existingKeys = Set.of("key1", "key2");
        when(redisTemplate.keys("*")).thenReturn(existingKeys);
        when(redisTemplate.hasKey("item1")).thenReturn(false);
        when(redisTemplate.hasKey("item2")).thenReturn(true);
        when(redisTemplate.hasKey("item3")).thenReturn(false);

        increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate, times(2)).expire(anyString(), eq(Duration.ofHours(2)));
        verify(redisTemplate).expire("key1", Duration.ofHours(2));
        verify(redisTemplate).expire("key2", Duration.ofHours(2));
        verify(valueOperations, times(2)).set(anyString(), anyString(), eq(Duration.ofHours(2)));
        verify(valueOperations).set("item1", "PreloadedValue_item1", Duration.ofHours(2));
        verify(valueOperations).set("item3", "PreloadedValue_item3", Duration.ofHours(2));
        verify(valueOperations, never()).set("item2", "PreloadedValue_item2", Duration.ofHours(2));
    }

    @Test
    void apply_WhenAllPreloadKeysExist_ShouldNotPreloadAnyKeys() {
        Set<String> existingKeys = Set.of("key1");
        when(redisTemplate.keys("*")).thenReturn(existingKeys);
        when(redisTemplate.hasKey("item1")).thenReturn(true);
        when(redisTemplate.hasKey("item2")).thenReturn(true);
        when(redisTemplate.hasKey("item3")).thenReturn(true);

        increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

        verify(redisTemplate).keys("*");
        verify(redisTemplate).expire("key1", Duration.ofHours(2));
        verify(valueOperations, never()).set(anyString(), anyString(), eq(Duration.ofHours(2)));
    }
}
