package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class CachePolicyTest {

    @Mock
    private CachePolicy cachePolicy;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Test
    void getExpirationTime_ShouldReturnDuration() {
        Duration expectedDuration = Duration.ofMinutes(30);
        when(cachePolicy.getExpirationTime()).thenReturn(expectedDuration);

        Duration result = cachePolicy.getExpirationTime();

        assertThat(result).isNotNull();
        verify(cachePolicy).getExpirationTime();
    }

    @Test
    void apply_ShouldExecuteWithRedisTemplateAndMeterRegistry() {
        cachePolicy.apply(redisTemplate, meterRegistry);

        verify(cachePolicy).apply(redisTemplate, meterRegistry);
    }
}
