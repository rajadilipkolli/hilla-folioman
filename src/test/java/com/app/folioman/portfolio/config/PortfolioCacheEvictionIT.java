package com.app.folioman.portfolio.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.common.AbstractIntegrationTest;
import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.app.folioman.portfolio.web.controller.UserTransactionsController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Integration tests for portfolio cache eviction functionality.
 *
 * These tests verify that:
 * 1. Transaction data is properly cached
 * 2. Cache eviction happens correctly at specified times
 * 3. Cache is refreshed with new data after eviction
 */
class PortfolioCacheEvictionIT extends AbstractIntegrationTest {

    private static final String TEST_PAN = "ABCDE1234F";
    private static final String TEST_CRON_EXPRESSION = "0 45 18 * * *";

    // Cache values for initial and updated data - using distinct values to clearly track changes
    private static final BigDecimal INITIAL_MONTHLY_VALUE = new BigDecimal("1234.56");
    private static final BigDecimal INITIAL_YEARLY_VALUE = new BigDecimal("9876.54");
    private static final BigDecimal UPDATED_MONTHLY_VALUE = new BigDecimal("5678.90");
    private static final BigDecimal UPDATED_YEARLY_VALUE = new BigDecimal("4321.09");

    @MockitoSpyBean
    private UserTransactionDetailsService userTransactionDetailsService;

    @MockitoSpyBean
    private PortfolioCacheConfig portfolioCacheConfig;

    @Autowired
    protected UserTransactionsController userTransactionsController;

    @Autowired
    protected PortfolioCacheProperties portfolioCacheProperties;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear any existing cache entries
        clearAllCaches();

        // Reset the mock
        reset(userTransactionDetailsService);

        // Set up initial test data
        setupInitialTestData();
    }

    @AfterEach
    void tearDown() {
        clearAllCaches();
        reset(userTransactionDetailsService);
    }

    @Test
    @DisplayName("Should evict transaction caches and refresh with new data")
    void shouldEvictTransactionCachesAndRefreshWithNewData() {
        // PHASE 1: Verify initial data loading and caching
        verifyInitialDataLoadedAndCached();

        // PHASE 2: Update the mock service to return new data
        setupUpdatedTestData();

        // PHASE 3: Verify cached data is still used before eviction
        verifyCachedDataIsStillUsed();

        // PHASE 4: Perform cache eviction and verify cache is cleared
        // Use the actual instance instead of the mock to perform the eviction
        portfolioCacheConfig.evictTransactionCaches();

        // Verify specific cache keys are gone
        Set<String> keysAfter = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean monthlyKeyGone = keysAfter.stream().noneMatch(key -> key.contains("monthly_" + TEST_PAN));
        boolean yearlyKeyGone = keysAfter.stream().noneMatch(key -> key.contains("yearly_" + TEST_PAN));

        assertThat(monthlyKeyGone).isTrue();
        assertThat(yearlyKeyGone).isTrue();

        // PHASE 5: Verify new data is loaded after cache eviction
        verifyNewDataIsLoadedAfterEviction();
    }

    @Test
    @DisplayName("Should configure cache eviction job with correct schedule")
    void shouldConfigureJobRunrCacheEvictionWithCorrectSchedule() {
        try (MockedStatic<BackgroundJob> mockedBackgroundJob = Mockito.mockStatic(BackgroundJob.class)) {
            // Use a simpler approach to verify the job scheduling
            PortfolioCacheProperties.Eviction evictionSpy = Mockito.spy(portfolioCacheProperties.getEviction());
            given(evictionSpy.getTransactionCron()).willReturn(TEST_CRON_EXPRESSION);

            // Create a new instance of the config for the test
            PortfolioCacheConfig configUnderTest =
                    new PortfolioCacheConfig(redisTemplate, new PortfolioCacheProperties() {
                        @Override
                        public Eviction getEviction() {
                            return evictionSpy;
                        }
                    });

            // Trigger the method that schedules the job
            configUnderTest.scheduleTransactionCacheEvictionJob(Mockito.mock(ApplicationStartedEvent.class));

            // Verify the job was scheduled with the correct parameters
            mockedBackgroundJob.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("transaction-cache-eviction"), eq(TEST_CRON_EXPRESSION), Mockito.any(JobLambda.class)));

            // Verify the cron expression was obtained from properties
            verify(evictionSpy, times(2)).getTransactionCron();
        }
    }

    /**
     * Set up the initial test data for the mock service
     */
    private void setupInitialTestData() {
        List<MonthlyInvestmentResponseDTO> monthlyResponses = Arrays.asList(
                new MonthlyInvestmentResponseDTO(2025, 3, BigDecimal.valueOf(10000), INITIAL_MONTHLY_VALUE),
                new MonthlyInvestmentResponseDTO(2025, 4, BigDecimal.valueOf(15000), BigDecimal.valueOf(25000)));

        List<YearlyInvestmentResponseDTO> yearlyResponses = Arrays.asList(
                new YearlyInvestmentResponseDTO(2024, INITIAL_YEARLY_VALUE),
                new YearlyInvestmentResponseDTO(2025, BigDecimal.valueOf(75000)));

        given(userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(anyString()))
                .willReturn(monthlyResponses);

        given(userTransactionDetailsService.getTotalInvestmentsByPanPerYear(anyString()))
                .willReturn(yearlyResponses);
    }

    /**
     * Update the mock service to return new test data
     */
    private void setupUpdatedTestData() {
        List<MonthlyInvestmentResponseDTO> newMonthlyResponses = Arrays.asList(
                new MonthlyInvestmentResponseDTO(2025, 3, BigDecimal.valueOf(20000), UPDATED_MONTHLY_VALUE),
                new MonthlyInvestmentResponseDTO(2025, 4, BigDecimal.valueOf(25000), BigDecimal.valueOf(45000)));

        List<YearlyInvestmentResponseDTO> newYearlyResponses = Arrays.asList(
                new YearlyInvestmentResponseDTO(2024, UPDATED_YEARLY_VALUE),
                new YearlyInvestmentResponseDTO(2025, BigDecimal.valueOf(85000)));

        given(userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(anyString()))
                .willReturn(newMonthlyResponses);

        given(userTransactionDetailsService.getTotalInvestmentsByPanPerYear(anyString()))
                .willReturn(newYearlyResponses);
    }

    /**
     * Verify that the initial data is loaded and cached properly
     */
    private void verifyInitialDataLoadedAndCached() {
        // Make initial calls to controller
        List<MonthlyInvestmentResponseDTO> initialMonthlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        List<YearlyInvestmentResponseDTO> initialYearlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify the data is as expected
        assertThat(initialMonthlyResult).isNotEmpty();
        assertThat(initialMonthlyResult.getFirst().cumulativeInvestment()).isEqualTo(INITIAL_MONTHLY_VALUE);
        assertThat(initialYearlyResult).isNotEmpty();
        assertThat(initialYearlyResult.getFirst().yearlyInvestment()).isEqualTo(INITIAL_YEARLY_VALUE);

        // Verify the service was called exactly once for each method
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerYear(TEST_PAN);
    }

    /**
     * Verify that cached data is still used before eviction
     */
    private void verifyCachedDataIsStillUsed() {
        // Make another call to controller - should use cached values
        List<MonthlyInvestmentResponseDTO> cachedMonthlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        List<YearlyInvestmentResponseDTO> cachedYearlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify we still get original values (from cache)
        assertThat(cachedMonthlyResult).isNotEmpty();
        assertThat(cachedMonthlyResult.getFirst().cumulativeInvestment()).isEqualTo(INITIAL_MONTHLY_VALUE);
        assertThat(cachedYearlyResult).isNotEmpty();
        assertThat(cachedYearlyResult.getFirst().yearlyInvestment()).isEqualTo(INITIAL_YEARLY_VALUE);

        // Verify service was not called again (still just once for each method)
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerYear(TEST_PAN);
    }

    /**
     * Verify that new data is loaded after cache eviction
     */
    private void verifyNewDataIsLoadedAfterEviction() {
        // Call controller methods again after eviction
        List<MonthlyInvestmentResponseDTO> newMonthlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        List<YearlyInvestmentResponseDTO> newYearlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify we now get updated values
        assertThat(newMonthlyResult).isNotEmpty();
        assertThat(newMonthlyResult.getFirst().cumulativeInvestment()).isEqualTo(UPDATED_MONTHLY_VALUE);
        assertThat(newYearlyResult).isNotEmpty();
        assertThat(newYearlyResult.getFirst().yearlyInvestment()).isEqualTo(UPDATED_YEARLY_VALUE);

        // Verify the service was called again (now twice for each method)
        verify(userTransactionDetailsService, times(2)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(2)).getTotalInvestmentsByPanPerYear(TEST_PAN);
    }

    /**
     * Clear all caches to ensure clean test state
     */
    private void clearAllCaches() {
        try {
            // Clear via CacheManager (preferred way)
            if (cacheManager.getCache(CacheNames.TRANSACTION_CACHE) != null) {
                Objects.requireNonNull(cacheManager.getCache(CacheNames.TRANSACTION_CACHE))
                        .clear();
            }

            // Also clear directly via Redis as fallback
            Set<String> keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Log exception but continue the test
            System.err.println("Error clearing caches: " + e.getMessage());
        }
    }
}
