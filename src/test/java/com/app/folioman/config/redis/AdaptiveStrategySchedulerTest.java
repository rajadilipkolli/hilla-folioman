package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private AdaptiveStrategyScheduler adaptiveStrategyScheduler;

    private Map<String, Object> testMetrics;

    @BeforeEach
    void setUp() {
        testMetrics = new HashMap<>();
        testMetrics.put("cacheSize", 1000);
        testMetrics.put("hitRate", 0.85);
        testMetrics.put("memoryUsage", 0.70);

        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "adaptiveStrategyIntervalMs", 600000L);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "stabilityThreshold", 3);
    }

    @Test
    void adaptStrategy_FirstExecution_ShouldApplyNewStrategy() {
        String newStrategy = "LRU_STRATEGY";
        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(newStrategy);
        when(policyRepository.getPolicy(newStrategy)).thenReturn(cachePolicy);

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

        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(strategy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());

        assertEquals(2, ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"));
    }

    @Test
    void adaptStrategy_SameStrategyAtThreshold_ShouldApplyStrategy() {
        String strategy = "LRU_STRATEGY";
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "lastAppliedStrategy", strategy);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "consecutiveStrategyMatches", 2);

        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(strategy);
        when(policyRepository.getPolicy(strategy)).thenReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);

        assertEquals(3, ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"));
    }

    @Test
    void adaptStrategy_DifferentStrategy_ShouldResetCounterAndApplyStrategy() {
        String oldStrategy = "LRU_STRATEGY";
        String newStrategy = "LFU_STRATEGY";
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "lastAppliedStrategy", oldStrategy);
        ReflectionTestUtils.setField(adaptiveStrategyScheduler, "consecutiveStrategyMatches", 2);

        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(newStrategy);
        when(policyRepository.getPolicy(newStrategy)).thenReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(newStrategy);
        verify(cacheAdapter).setPolicy(cachePolicy);

        assertEquals(0, ReflectionTestUtils.getField(adaptiveStrategyScheduler, "consecutiveStrategyMatches"));
        assertEquals(newStrategy, ReflectionTestUtils.getField(adaptiveStrategyScheduler, "lastAppliedStrategy"));
    }

    @Test
    void adaptStrategy_ExceptionInMonitorGetMetrics_ShouldCatchAndLog() {
        when(monitor.getMetrics()).thenThrow(new RuntimeException("Monitor error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator, never()).evaluate(any());
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInEvaluator_ShouldCatchAndLog() {
        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenThrow(new RuntimeException("Evaluator error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository, never()).getPolicy(anyString());
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInPolicyRepository_ShouldCatchAndLog() {
        String strategy = "LRU_STRATEGY";
        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(strategy);
        when(policyRepository.getPolicy(strategy)).thenThrow(new RuntimeException("Policy error"));

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter, never()).setPolicy(any());
    }

    @Test
    void adaptStrategy_ExceptionInCacheAdapter_ShouldCatchAndLog() {
        String strategy = "LRU_STRATEGY";
        when(monitor.getMetrics()).thenReturn(testMetrics);
        when(evaluator.evaluate(testMetrics)).thenReturn(strategy);
        when(policyRepository.getPolicy(strategy)).thenReturn(cachePolicy);
        doThrow(new RuntimeException("Cache adapter error")).when(cacheAdapter).setPolicy(cachePolicy);

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(testMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);
    }

    @Test
    void adaptStrategy_NullMetrics_ShouldHandleGracefully() {
        when(monitor.getMetrics()).thenReturn(null);

        assertDoesNotThrow(() -> adaptiveStrategyScheduler.adaptStrategy());

        verify(monitor).getMetrics();
        verify(evaluator, never()).evaluate(null);
        verify(policyRepository, never()).getPolicy("DEFAULT_STRATEGY");
        verify(cacheAdapter, never()).setPolicy(cachePolicy);
    }

    @Test
    void constructor_WithValidDependencies_ShouldCreateInstance() {
        AdaptiveStrategyScheduler scheduler =
                new AdaptiveStrategyScheduler(cacheAdapter, monitor, evaluator, policyRepository);

        assertNotNull(scheduler);
    }

    @Test
    void adaptStrategy_EmptyMetrics_ShouldProcessSuccessfully() {
        Map<String, Object> emptyMetrics = new HashMap<>();
        String strategy = "EMPTY_STRATEGY";

        when(monitor.getMetrics()).thenReturn(emptyMetrics);
        when(evaluator.evaluate(emptyMetrics)).thenReturn(strategy);
        when(policyRepository.getPolicy(strategy)).thenReturn(cachePolicy);

        adaptiveStrategyScheduler.adaptStrategy();

        verify(monitor).getMetrics();
        verify(evaluator).evaluate(emptyMetrics);
        verify(policyRepository).getPolicy(strategy);
        verify(cacheAdapter).setPolicy(cachePolicy);
    }
}
