package com.app.folioman.portfolio.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.common.AbstractIntegrationTest;
import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import com.app.folioman.portfolio.web.controller.UserTransactionsController;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class PortfolioCacheEvictionIntTest extends AbstractIntegrationTest {

    private static final String TEST_PAN = "ABCDE1234F";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PortfolioCacheConfig portfolioCacheConfig;

    @MockitoSpyBean
    private UserTransactionDetailsService userTransactionDetailsService;

    @Autowired
    private UserTransactionsController userTransactionsController;

    @BeforeEach
    void setUp() {
        // First, clear any existing cache entries
        clearTransactionCaches();

        // Reset the mock completely
        reset(userTransactionDetailsService);

        // Create some test data
        List<MonthlyInvestmentResponseDTO> monthlyResponses = Arrays.asList(
                new MonthlyInvestmentResponseDTO(2025, 3, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)),
                new MonthlyInvestmentResponseDTO(2025, 4, BigDecimal.valueOf(15000), BigDecimal.valueOf(25000)));

        List<YearlyInvestmentResponseDTO> yearlyResponses = Arrays.asList(
                new YearlyInvestmentResponseDTO(2024, BigDecimal.valueOf(50000)),
                new YearlyInvestmentResponseDTO(2025, BigDecimal.valueOf(75000)));

        // Mock service responses
        when(userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(TEST_PAN))
                .thenReturn(monthlyResponses);

        when(userTransactionDetailsService.getTotalInvestmentsByPanPerYear(TEST_PAN))
                .thenReturn(yearlyResponses);
    }

    @Test
    void shouldEvictTransactionCachesAtScheduledTime() {
        // First phase: Populate the cache
        List<MonthlyInvestmentResponseDTO> monthlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        List<YearlyInvestmentResponseDTO> yearlyResult =
                userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify data was returned
        assertThat(monthlyResult).hasSize(2);
        assertThat(yearlyResult).hasSize(2);

        // Verify service was called to populate cache
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Clear mock tracking before second phase
        clearInvocations(userTransactionDetailsService);

        // Second phase: Verify cache is being used

        // Call again - this should use cached values
        monthlyResult = userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        yearlyResult = userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify data is still correct
        assertThat(monthlyResult).hasSize(2);
        assertThat(yearlyResult).hasSize(2);

        // Verify service was NOT called again (cache hit)
        verify(userTransactionDetailsService, times(0)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(0)).getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify cache contains the expected keys
        Set<String> keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        assertThat(keys).isNotNull();
        assertThat(keys.size()).isGreaterThanOrEqualTo(2); // At least our 2 keys

        boolean hasMonthlyKey = keys.stream().anyMatch(key -> key.contains("monthly_" + TEST_PAN));
        boolean hasYearlyKey = keys.stream().anyMatch(key -> key.contains("yearly_" + TEST_PAN));

        assertThat(hasMonthlyKey).isTrue();
        assertThat(hasYearlyKey).isTrue();

        // Third phase: Verify cache eviction

        // Manually trigger the scheduled cache eviction method
        portfolioCacheConfig.evictTransactionCaches();

        // Verify cache no longer contains the keys
        keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        if (!keys.isEmpty()) {
            // If there are keys left, check our specific keys are gone
            boolean monthlyKeyRemoved = keys.stream().noneMatch(key -> key.contains("monthly_" + TEST_PAN));
            boolean yearlyKeyRemoved = keys.stream().noneMatch(key -> key.contains("yearly_" + TEST_PAN));

            assertThat(monthlyKeyRemoved).isTrue();
            assertThat(yearlyKeyRemoved).isTrue();
        }
        // If keys is null or empty, then all cache entries were removed, which is also valid

        // Fourth phase: Verify service is called again after eviction

        // Clear mock tracking again
        clearInvocations(userTransactionDetailsService);

        // Call the endpoints again - service should be called again since cache was cleared
        monthlyResult = userTransactionsController.getTotalInvestmentsByPanPerMonth(TEST_PAN);
        yearlyResult = userTransactionsController.getTotalInvestmentsByPanPerYear(TEST_PAN);

        // Verify data is still correct
        assertThat(monthlyResult).hasSize(2);
        assertThat(yearlyResult).hasSize(2);

        // Verify service was called after cache eviction
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerMonth(TEST_PAN);
        verify(userTransactionDetailsService, times(1)).getTotalInvestmentsByPanPerYear(TEST_PAN);
    }

    @Test
    void shouldEvictCachesAtCorrectTime() {
        // Verify that the cron expression is set to run at 12:15 AM IST
        try {
            // Use reflection to get the scheduled annotation on the method
            Method method = PortfolioCacheConfig.class.getMethod("evictTransactionCaches");
            org.springframework.scheduling.annotation.Scheduled annotation =
                    method.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.cron()).isEqualTo("0 15 0 * * *");
            assertThat(annotation.zone()).isEqualTo("Asia/Kolkata");

            // This confirms the schedule is set for 12:15 AM Indian Standard Time
        } catch (NoSuchMethodException e) {
            throw new AssertionError("evictTransactionCaches method not found", e);
        }
    }

    private void clearTransactionCaches() {
        try {
            Set<String> keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Log exception but continue the test
            System.err.println("Error clearing transaction caches: " + e.getMessage());
        }
    }
}
