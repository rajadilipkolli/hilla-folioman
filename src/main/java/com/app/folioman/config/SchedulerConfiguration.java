package com.app.folioman.config;

import com.app.folioman.config.redis.AdaptiveStrategyScheduler;
import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.domain.MfSchemeSyncService;
import com.app.folioman.portfolio.UserSchemeDetailService;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Centralized scheduler configuration for the application.
 * All scheduled jobs in the application are configured here using JobRunr for consistent management.
 */
@Configuration
public class SchedulerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private final UserSchemeDetailService userSchemeDetailService;
    private final MFNavService mfNavService;
    private final AdaptiveStrategyScheduler adaptiveStrategyScheduler;
    private final SchedulerProperties schedulerProperties;
    private final MfSchemeSyncService mfSchemeSyncService;

    public SchedulerConfiguration(
            UserSchemeDetailService userSchemeDetailsService,
            MFNavService mfNavService,
            AdaptiveStrategyScheduler adaptiveStrategyScheduler,
            SchedulerProperties schedulerProperties,
            MfSchemeSyncService mfSchemeSyncService) {
        this.userSchemeDetailService = userSchemeDetailsService;
        this.mfNavService = mfNavService;
        this.adaptiveStrategyScheduler = adaptiveStrategyScheduler;
        this.schedulerProperties = schedulerProperties;
        this.mfSchemeSyncService = mfSchemeSyncService;
    }

    @Bean
    public SchemeSyncRetryFilter schemeSyncRetryFilter() {
        return new SchemeSyncRetryFilter();
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleAllJobs(ApplicationStartedEvent event) {
        scheduleSetAMFIIfNullJob();
        scheduleNavDataJobs();
        scheduleAdaptiveStrategyJob();
        scheduleSchemeSyncJob();
        // Portfolio cache eviction job is scheduled separately in the portfolio module
    }

    private void scheduleSchemeSyncJob() {
        LOGGER.info("Scheduling scheme sync job with cron: {}", schedulerProperties.getSchemeSyncJobCron());
        BackgroundJob.scheduleRecurrently(
                "update-mf-schemes", schedulerProperties.getSchemeSyncJobCron(), mfSchemeSyncService::syncAllSchemes);
        LOGGER.info("scheme sync job scheduled successfully");
    }

    private void scheduleSetAMFIIfNullJob() {
        LOGGER.info("Scheduling setAMFIIfNull job with cron: {}", schedulerProperties.getAmfiJobCron());
        BackgroundJob.scheduleRecurrently(
                "user-scheme-amfi-update",
                schedulerProperties.getAmfiJobCron(),
                userSchemeDetailService::setUserSchemeAMFIIfNull);
        LOGGER.info("setAMFIIfNull job scheduled successfully");
    }

    private void scheduleNavDataJobs() {
        LOGGER.info("Scheduling loadHistoricalNavJob with cron: {}", schedulerProperties.getHistoricalNavJobCron());
        BackgroundJob.scheduleRecurrently(
                "historical-nav-load",
                schedulerProperties.getHistoricalNavJobCron(),
                mfNavService::loadHistoricalDataIfNotExists);
        LOGGER.info("loadHistoricalNavJob scheduled successfully");

        LOGGER.info("Scheduling loadLastDayDataNav with cron: {}", schedulerProperties.getDailyDataJobCron());
        BackgroundJob.scheduleRecurrently(
                "daily-nav-load", schedulerProperties.getDailyDataJobCron(), mfNavService::loadLastDayDataNav);
        LOGGER.info("loadLastDayDataNav scheduled successfully");
    }

    private void scheduleAdaptiveStrategyJob() {
        LOGGER.info("Scheduling adaptive strategy job with cron: {}", schedulerProperties.getAdaptiveStrategyJobCron());
        BackgroundJob.scheduleRecurrently(
                "adaptive-cache-strategy",
                schedulerProperties.getAdaptiveStrategyJobCron(),
                adaptiveStrategyScheduler::adaptStrategy);
        LOGGER.info("Adaptive strategy job scheduled successfully");
    }
}
