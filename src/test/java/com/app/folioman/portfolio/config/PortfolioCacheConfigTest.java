package com.app.folioman.portfolio.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.config.redis.CacheNames;
import java.util.Collections;
import java.util.Set;
import org.jobrunr.scheduling.BackgroundJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class PortfolioCacheConfigTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PortfolioCacheProperties portfolioCacheProperties;

    @Mock
    private PortfolioCacheProperties.Eviction eviction;

    @Mock
    private ApplicationStartedEvent applicationStartedEvent;

    @InjectMocks
    private PortfolioCacheConfig portfolioCacheConfig;

    @Test
    void constructor_ShouldInitializeFields() {
        PortfolioCacheConfig config = new PortfolioCacheConfig(redisTemplate, portfolioCacheProperties);

        verify(portfolioCacheProperties, never()).getEviction();
    }

    @Test
    void scheduleTransactionCacheEvictionJob_ShouldScheduleJobWithCronExpression() {
        String cronExpression = "0 0 2 * * ?";
        when(portfolioCacheProperties.getEviction()).thenReturn(eviction);
        when(eviction.getTransactionCron()).thenReturn(cronExpression);

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            portfolioCacheConfig.scheduleTransactionCacheEvictionJob(applicationStartedEvent);

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("transaction-cache-eviction"),
                    eq(cronExpression),
                    any(org.jobrunr.jobs.lambdas.JobLambda.class)));
        }

        org.mockito.Mockito.verify(eviction, org.mockito.Mockito.atLeastOnce()).getTransactionCron();
    }

    @Test
    void evictTransactionCaches_WithNoKeys_ShouldReturnEarly() {
        when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

        portfolioCacheConfig.evictTransactionCaches();

        verify(redisTemplate).keys(CacheNames.TRANSACTION_CACHE + "::*");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void evictTransactionCaches_WithMatchingKeys_ShouldEvictCorrectKeys() {
        Set<String> keys = Set.of(
                CacheNames.TRANSACTION_CACHE + "::monthly_2023",
                CacheNames.TRANSACTION_CACHE + "::yearly_2023",
                CacheNames.TRANSACTION_CACHE + "::daily_2023",
                CacheNames.TRANSACTION_CACHE + "::weekly_2023");
        when(redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*")).thenReturn(keys);
        when(portfolioCacheProperties.getEviction()).thenReturn(eviction);
        when(eviction.getBatchSize()).thenReturn(10);

        portfolioCacheConfig.evictTransactionCaches();
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::monthly_2023");
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::yearly_2023");
        verify(redisTemplate, never()).delete(CacheNames.TRANSACTION_CACHE + "::daily_2023");
        verify(redisTemplate, never()).delete(CacheNames.TRANSACTION_CACHE + "::weekly_2023");
    }

    @Test
    void evictTransactionCaches_WithBatchProcessing_ShouldProcessInBatches() {
        Set<String> keys = Set.of(
                CacheNames.TRANSACTION_CACHE + "::monthly_1",
                CacheNames.TRANSACTION_CACHE + "::monthly_2",
                CacheNames.TRANSACTION_CACHE + "::monthly_3");
        when(redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*")).thenReturn(keys);
        when(portfolioCacheProperties.getEviction()).thenReturn(eviction);
        when(eviction.getBatchSize()).thenReturn(2);

        portfolioCacheConfig.evictTransactionCaches();

        verify(redisTemplate, times(3)).delete(anyString());
        verify(eviction, atLeastOnce()).getBatchSize();
    }

    @Test
    void evictTransactionCaches_WithException_ShouldHandleGracefully() {
        when(redisTemplate.keys(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        portfolioCacheConfig.evictTransactionCaches();

        verify(redisTemplate).keys(CacheNames.TRANSACTION_CACHE + "::*");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void evictTransactionCaches_WithDeleteException_ShouldHandleGracefully() {
        Set<String> keys = Set.of(CacheNames.TRANSACTION_CACHE + "::monthly_2023");
        when(redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*")).thenReturn(keys);
        doThrow(new RuntimeException("Delete failed")).when(redisTemplate).delete(anyString());

        portfolioCacheConfig.evictTransactionCaches();

        verify(redisTemplate).keys(CacheNames.TRANSACTION_CACHE + "::*");
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::monthly_2023");
    }

    @Test
    void evictTransactionCaches_WithOnlyNonMatchingKeys_ShouldNotEvictAny() {
        Set<String> keys = Set.of(
                CacheNames.TRANSACTION_CACHE + "::daily_2023",
                CacheNames.TRANSACTION_CACHE + "::weekly_2023",
                CacheNames.TRANSACTION_CACHE + "::custom_2023");
        when(redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*")).thenReturn(keys);

        portfolioCacheConfig.evictTransactionCaches();
        verify(redisTemplate).keys(CacheNames.TRANSACTION_CACHE + "::*");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void evictTransactionCaches_WithMixedKeys_ShouldOnlyEvictMatching() {
        Set<String> keys = Set.of(
                CacheNames.TRANSACTION_CACHE + "::monthly_jan",
                CacheNames.TRANSACTION_CACHE + "::yearly_summary",
                CacheNames.TRANSACTION_CACHE + "::daily_report",
                CacheNames.TRANSACTION_CACHE + "::some_monthly_data",
                CacheNames.TRANSACTION_CACHE + "::yearly_stats");
        when(redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*")).thenReturn(keys);
        when(portfolioCacheProperties.getEviction()).thenReturn(eviction);
        when(eviction.getBatchSize()).thenReturn(5);

        portfolioCacheConfig.evictTransactionCaches();

        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::monthly_jan");
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::yearly_summary");
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::some_monthly_data");
        verify(redisTemplate).delete(CacheNames.TRANSACTION_CACHE + "::yearly_stats");
        verify(redisTemplate, never()).delete(CacheNames.TRANSACTION_CACHE + "::daily_report");
    }
}
