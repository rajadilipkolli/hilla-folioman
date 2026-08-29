package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyRepositoryTest {

    @InjectMocks
    private PolicyRepository policyRepository;

    @Test
    void getPolicy_shouldReturnReduceCacheSizePolicy_whenStrategyIsReduceCacheSize() {
        CachePolicy policy = policyRepository.getPolicy("REDUCE_CACHE_SIZE");
        assertThat(policy).isInstanceOf(ReduceCacheSizePolicy.class);
    }

    @Test
    void getPolicy_shouldReturnIncreaseCacheSizePolicy_whenStrategyIsIncreaseCacheSize() {
        CachePolicy policy = policyRepository.getPolicy("INCREASE_CACHE_SIZE");
        assertThat(policy).isInstanceOf(IncreaseCacheSizePolicy.class);
    }

    @Test
    void getPolicy_shouldReturnAdjustTTLPolicy_whenStrategyIsAdjustTTL() {
        CachePolicy policy = policyRepository.getPolicy("ADJUST_TTL");
        assertThat(policy).isInstanceOf(AdjustTTLPolicy.class);
    }

    @Test
    void getPolicy_shouldReturnDefaultPolicy_whenStrategyIsNotRecognized() {
        CachePolicy policy = policyRepository.getPolicy("UNKNOWN");
        assertThat(policy).isInstanceOf(DefaultPolicy.class);
    }
}
