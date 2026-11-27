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

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class IncreaseCacheSizePolicyTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private IncreaseCacheSizePolicy increaseCacheSizePolicy;

    @Test
    void getExpirationTime_ShouldReturnTwoHours() {
        Duration result = increaseCacheSizePolicy.getExpirationTime();
        assertThat(result).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void apply_WhenCacheIsEmpty_ShouldReturnEarly() {
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(Collections.emptySet());
        })) {
            increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().getFirst()).scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
            verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        }
    }

    @Test
    void apply_WhenKeysIsNull_ShouldReturnEarly() {
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(null);
        })) {
            increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().getFirst()).scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
            verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        }
    }

    @Test
    void apply_WhenCacheHasKeys_ShouldIncreaseTtl() {
        Set<String> existingKeys = Set.of("folioman:cache:c1::key1", "folioman:cache:c1::key2");
        try (MockedConstruction<Monitor> mocked = mockConstruction(Monitor.class, (mock, ctx) -> {
            when(mock.scanKeys(Monitor.CACHE_KEY_PREFIX + "*")).thenReturn(existingKeys);
        })) {
            increaseCacheSizePolicy.apply(redisTemplate, meterRegistry);

            verify(mocked.constructed().getFirst()).scanKeys(Monitor.CACHE_KEY_PREFIX + "*");
            verify(redisTemplate, times(2)).expire(anyString(), eq(Duration.ofHours(2)));
            verify(redisTemplate).expire("folioman:cache:c1::key1", Duration.ofHours(2));
            verify(redisTemplate).expire("folioman:cache:c1::key2", Duration.ofHours(2));
        }
    }
}
