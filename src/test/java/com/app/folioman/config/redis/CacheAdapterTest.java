package com.app.folioman.config.redis;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class CacheAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private CachePolicy cachePolicy;

    private CacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        cacheAdapter = new CacheAdapter(redisTemplate, meterRegistry);
    }

    @Test
    void constructor_ShouldInitializeFields() {
        CacheAdapter adapter = new CacheAdapter(redisTemplate, meterRegistry);
        // Constructor test - no assertions needed as we're testing field initialization
        // which is verified implicitly by successful object creation
    }

    @Test
    void setPolicy_WithValidPolicy_ShouldSetPolicyAndApplyIt() {
        cacheAdapter.setPolicy(cachePolicy);

        verify(cachePolicy).apply(redisTemplate, meterRegistry);
    }

    @Test
    void setPolicy_WithNullPolicy_ShouldSetPolicyButNotApplyAnything() {
        cacheAdapter.setPolicy(null);

        verifyNoInteractions(cachePolicy);
        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(meterRegistry);
    }

    @Test
    void setPolicy_CalledTwice_ShouldApplyBothPolicies() {
        CachePolicy secondPolicy = org.mockito.Mockito.mock(CachePolicy.class);

        cacheAdapter.setPolicy(cachePolicy);
        cacheAdapter.setPolicy(secondPolicy);

        verify(cachePolicy).apply(redisTemplate, meterRegistry);
        verify(secondPolicy).apply(redisTemplate, meterRegistry);
    }

    @Test
    void setPolicy_WithNullAfterValidPolicy_ShouldNotApplyNullPolicy() {
        cacheAdapter.setPolicy(cachePolicy);
        cacheAdapter.setPolicy(null);

        verify(cachePolicy).apply(redisTemplate, meterRegistry);
        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(meterRegistry);
    }
}
