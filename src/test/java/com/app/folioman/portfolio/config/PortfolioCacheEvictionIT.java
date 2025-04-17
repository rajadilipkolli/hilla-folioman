package com.app.folioman.portfolio.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.common.AbstractIntegrationTest;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.context.event.ApplicationStartedEvent;

/**
 * Integration tests for portfolio cache eviction functionality.
 *
 * These tests verify that:
 * 1. Cache eviction happens correctly at specified times
 */
class PortfolioCacheEvictionIT extends AbstractIntegrationTest {

    private static final String TEST_CRON_EXPRESSION = "0 45 18 * * *";

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
}
