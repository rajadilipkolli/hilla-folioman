package com.app.folioman.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.config.redis.AdaptiveStrategyScheduler;
import com.app.folioman.portfolio.UserSchemeDetailService;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;

@ExtendWith(MockitoExtension.class)
class SchedulerConfigurationTest {

    @Mock
    private UserSchemeDetailService userSchemeDetailService;

    @Mock
    private AdaptiveStrategyScheduler adaptiveStrategyScheduler;

    @Mock
    private SchedulerProperties schedulerProperties;

    @Mock
    private ApplicationStartedEvent applicationStartedEvent;

    private SchedulerConfiguration schedulerConfiguration;

    @BeforeEach
    void setUp() {
        schedulerConfiguration =
                new SchedulerConfiguration(userSchemeDetailService, adaptiveStrategyScheduler, schedulerProperties);
    }

    @Test
    void scheduleAllJobs_ShouldScheduleAllJobs() {
        String amfiCron = "0 0 1 * * ?";
        String adaptiveStrategyCron = "0 0 4 * * ?";

        when(schedulerProperties.getAmfiJobCron()).thenReturn(amfiCron);
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn(adaptiveStrategyCron);

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("user-scheme-amfi-update"), eq(amfiCron), any(JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("adaptive-cache-strategy"), eq(adaptiveStrategyCron), any(JobLambda.class)));
        }

        // Each getter is invoked once for logging and once when passed to BackgroundJob.scheduleRecurrently
        verify(schedulerProperties, times(2)).getAmfiJobCron();
        verify(schedulerProperties, times(2)).getAdaptiveStrategyJobCron();
    }

    @Test
    void scheduleAllJobs_ShouldCallAllPrivateMethods() {
        when(schedulerProperties.getAmfiJobCron()).thenReturn("0 0 1 * * ?");
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn("0 0 4 * * ?");

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(
                    () -> BackgroundJob.scheduleRecurrently(anyString(), anyString(), any(JobLambda.class)), times(2));
        }
    }

    @Test
    void scheduleAllJobs_ShouldUseCorrectJobNames() {
        when(schedulerProperties.getAmfiJobCron()).thenReturn("0 0 1 * * ?");
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn("0 0 4 * * ?");

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("user-scheme-amfi-update"), anyString(), any(JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("adaptive-cache-strategy"), anyString(), any(JobLambda.class)));
        }
    }
}
