package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdaptiveStrategySchedulerTest {

    @Mock
    private CacheAdapter cacheAdapter;

    @Mock
    private Monitor monitor;

    @Mock
    private Evaluator evaluator;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CachePolicy cachePolicy;

    @Mock
    private RedisAppProperties redisAppProperties;

    @Mock
    private RedisAppProperties.AdaptiveStrategy adaptiveStrategy;

    @InjectMocks
    private AdaptiveStrategyScheduler adaptiveStrategyScheduler;

    private Map<String, Object> testMetrics;

    @BeforeEach
    void setUp() {
        testMetrics = new HashMap<>();
        testMetrics.put("cacheSize", 1000);
        testMetrics.put("hitRate", 0.85);
        testMetrics.put("memoryUsage", 0.70);
    }

    @Test
    void adaptStrategy_FirstExecution_ShouldApplyNewStrategy() {
        String newStrategy = "LRU_STRATEGY";
        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(newStrategy);
        given(policyRepository.getPolicy(newStrategy)).willReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(newStrategy);
        verify(cacheAdapter).setPolicy(cachePolicy);
    }

    @Test
    void adaptStrategy_SameStrategyBelowThreshold_ShouldSkipApplication() {
        String strategy = "LRU_STRATEGY";
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "lastAppliedStrategy", strategy);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "consecutiveStrategyMatches", 1);

        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(strategy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());

        assertThat(ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"))
                .isEqualTo(2);
    }

    @Test
    void adaptStrategy_SameStrategyAtThreshold_ShouldApplyStrategy() {

        given(redisAppProperties.getAdaptiveStrategy()).willReturn(adaptiveStrategy);
        given(adaptiveStrategy.getStabilityThreshold()).willReturn(3);

        String strategy = "LRU_STRATEGY";
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "lastAppliedStrategy", strategy);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "consecutiveStrategyMatches", 2);

        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(strategy);
        given(policyRepository.getPolicy(strategy)).willReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);

        assertThat(ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"))
                .isEqualTo(3);
    }

    @Test
    void adaptStrategy_DifferentStrategy_ShouldResetCounterAndApplyStrategy() {
        String oldStrategy = "LRU_STRATEGY";
        String newStrategy = "LFU_STRATEGY";
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "lastAppliedStrategy", oldStrategy);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "consecutiveStrategyMatches", 2);

        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(newStrategy);
        given(policyRepository.getPolicy(newStrategy)).willReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(newStrategy);
        verify(cacheAdapter).setPolicy(cachePolicy);

        assertThat(ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"))
                .isEqualTo(0);
        assertThat(ReflectionTestUtils.getField(adaptiveStrategyScheduler, "lastAppliedStrategy"))
                .isEqualTo(newStrategy);
    }

    @Test
    void adaptStrategy_ExceptionInMonitorGetMetrics_ShouldCatchAndLog() {
        given(monitor.getMetrics()).willThrow(new RuntimeException("Monitor error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator, never()).evaluate(any());
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInEvaluator_ShouldCatchAndLog() {
        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willThrow(new RuntimeException("Evaluator error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInPolicyRepository_ShouldCatchAndLog() {
        String strategy = "LRU_STRATEGY";
        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(strategy);
        given(policyRepository.getPolicy(strategy)).willThrow(new RuntimeException("Policy error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInCacheAdapter_ShouldCatchAndLog() {
        String strategy = "LRU_STRATEGY";
        given(monitor.getMetrics()).willReturn(testMetrics);
        given(evaluator.evaluate(testMetrics)).willReturn(strategy);
        given(policyRepository.getPolicy(strategy)).willReturn(cachePolicy);
        willThrow(new RuntimeException("Cache adapter error"))
                .given(cacheAdapter)
                .setPolicy(cachePolicy);

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);
    }

    @Test
    void adaptStrategy_NullMetrics_ShouldHandleGracefully() {
        given(monitor.getMetrics()).willReturn(null);

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator, never()).evaluate(any());
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any(CachePolicy.class));
    }

    @Test
    void adaptStrategy_EmptyMetrics_ShouldProcessSuccessfully() {
        Map<String, Object> emptyMetrics = new HashMap<>();
        String strategy = "EMPTY_STRATEGY";

        given(monitor.getMetrics()).willReturn(emptyMetrics);
        given(evaluator.evaluate(emptyMetrics)).willReturn(strategy);
        given(policyRepository.getPolicy(strategy)).willReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(emptyMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);
    }
}
