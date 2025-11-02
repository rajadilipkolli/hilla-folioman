package com.app.folioman.config;

import static org.mockito.Mockito.*;

import com.app.folioman.config.redis.AdaptiveStrategyScheduler;
import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.portfolio.UserSchemeDetailService;
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
    private MFNavService mfNavService;

    @Mock
    private AdaptiveStrategyScheduler adaptiveStrategyScheduler;

    @Mock
    private SchedulerProperties schedulerProperties;

    @Mock
    private ApplicationStartedEvent applicationStartedEvent;

    private SchedulerConfiguration schedulerConfiguration;

    @BeforeEach
    void setUp() {
        schedulerConfiguration = new SchedulerConfiguration(
                userSchemeDetailService, mfNavService, adaptiveStrategyScheduler, schedulerProperties);
    }

    @Test
    void constructor_ShouldInitializeAllDependencies() {
        SchedulerConfiguration config = new SchedulerConfiguration(
                userSchemeDetailService, mfNavService, adaptiveStrategyScheduler, schedulerProperties);

        // Verify that the configuration is created without throwing exceptions
        // Dependencies are stored and can be used
        verifyNoInteractions(userSchemeDetailService, mfNavService, adaptiveStrategyScheduler, schedulerProperties);
    }

    @Test
    void scheduleAllJobs_ShouldScheduleAllFourJobs() {
        String amfiCron = "0 0 1 * * ?";
        String historicalNavCron = "0 0 2 * * ?";
        String dailyDataCron = "0 0 3 * * ?";
        String adaptiveStrategyCron = "0 0 4 * * ?";

        when(schedulerProperties.getAmfiJobCron()).thenReturn(amfiCron);
        when(schedulerProperties.getHistoricalNavJobCron()).thenReturn(historicalNavCron);
        when(schedulerProperties.getDailyDataJobCron()).thenReturn(dailyDataCron);
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn(adaptiveStrategyCron);

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("user-scheme-amfi-update"), eq(amfiCron), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("historical-nav-load"), eq(historicalNavCron), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("daily-nav-load"), eq(dailyDataCron), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("adaptive-cache-strategy"),
                    eq(adaptiveStrategyCron),
                    any(org.jobrunr.jobs.lambdas.JobLambda.class)));
        }

        // Each getter is invoked once for logging and once when passed to BackgroundJob.scheduleRecurrently
        verify(schedulerProperties, times(2)).getAmfiJobCron();
        verify(schedulerProperties, times(2)).getHistoricalNavJobCron();
        verify(schedulerProperties, times(2)).getDailyDataJobCron();
        verify(schedulerProperties, times(2)).getAdaptiveStrategyJobCron();
    }

    @Test
    void scheduleAllJobs_ShouldCallAllPrivateMethods() {
        when(schedulerProperties.getAmfiJobCron()).thenReturn("0 0 1 * * ?");
        when(schedulerProperties.getHistoricalNavJobCron()).thenReturn("0 0 2 * * ?");
        when(schedulerProperties.getDailyDataJobCron()).thenReturn("0 0 3 * * ?");
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn("0 0 4 * * ?");

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(
                    () -> BackgroundJob.scheduleRecurrently(
                            anyString(), anyString(), any(org.jobrunr.jobs.lambdas.JobLambda.class)),
                    times(4));
        }
    }

    @Test
    void scheduleAllJobs_ShouldUseCorrectJobNames() {
        when(schedulerProperties.getAmfiJobCron()).thenReturn("0 0 1 * * ?");
        when(schedulerProperties.getHistoricalNavJobCron()).thenReturn("0 0 2 * * ?");
        when(schedulerProperties.getDailyDataJobCron()).thenReturn("0 0 3 * * ?");
        when(schedulerProperties.getAdaptiveStrategyJobCron()).thenReturn("0 0 4 * * ?");

        try (MockedStatic<BackgroundJob> backgroundJobMock = mockStatic(BackgroundJob.class)) {
            schedulerConfiguration.scheduleAllJobs(applicationStartedEvent);

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("user-scheme-amfi-update"), anyString(), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("historical-nav-load"), anyString(), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("daily-nav-load"), anyString(), any(org.jobrunr.jobs.lambdas.JobLambda.class)));

            backgroundJobMock.verify(() -> BackgroundJob.scheduleRecurrently(
                    eq("adaptive-cache-strategy"), anyString(), any(org.jobrunr.jobs.lambdas.JobLambda.class)));
        }
    }
}
