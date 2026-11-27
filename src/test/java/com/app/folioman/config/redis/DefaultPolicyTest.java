package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyTest {

    @InjectMocks
    private DefaultPolicy defaultPolicy;

    @Test
    void getExpirationTime_ShouldReturnFifteenMinutes() {
        Duration result = defaultPolicy.getExpirationTime();

        assertThat(result).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void apply_ShouldExecuteWithoutError() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) mock(RedisTemplate.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        defaultPolicy.apply(redisTemplate, meterRegistry);
        // DefaultPolicy does no work; assert there are no interactions with redisTemplate or meterRegistry
        verifyNoInteractions(redisTemplate, meterRegistry);
    }
}
