package com.app.folioman.config;

import com.app.folioman.config.redis.AdaptiveStrategyScheduler;
import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.portfolio.UserSchemeDetailService;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Centralized scheduler configuration for the application.
 * All scheduled jobs in the application are configured here using JobRunr for consistent management.
 */
@Configuration
public class SchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private final UserSchemeDetailService userSchemeDetailService;
    private final MFNavService mfNavService;
    private final AdaptiveStrategyScheduler adaptiveStrategyScheduler;
    private final SchedulerProperties schedulerProperties;

    public SchedulerConfiguration(
            UserSchemeDetailService userSchemeDetailsService,
            MFNavService mfNavService,
            AdaptiveStrategyScheduler adaptiveStrategyScheduler,
            SchedulerProperties schedulerProperties) {
        this.userSchemeDetailService = userSchemeDetailsService;
        this.mfNavService = mfNavService;
        this.adaptiveStrategyScheduler = adaptiveStrategyScheduler;
        this.schedulerProperties = schedulerProperties;
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleAllJobs(ApplicationStartedEvent event) {
        scheduleSetAMFIIfNullJob();
        scheduleNavDataJobs();
        scheduleAdaptiveStrategyJob();
        // Portfolio cache eviction job is scheduled separately in the portfolio module
    }

    private void scheduleSetAMFIIfNullJob() {
        log.info("Scheduling setAMFIIfNull job with cron: {}", schedulerProperties.getAmfiJobCron());
        BackgroundJob.scheduleRecurrently(
                "user-scheme-amfi-update",
                schedulerProperties.getAmfiJobCron(),
                userSchemeDetailService::setUserSchemeAMFIIfNull);
        log.info("setAMFIIfNull job scheduled successfully");
    }

    private void scheduleNavDataJobs() {
        log.info("Scheduling loadHistoricalNavJob with cron: {}", schedulerProperties.getHistoricalNavJobCron());
        BackgroundJob.scheduleRecurrently(
                "historical-nav-load",
                schedulerProperties.getHistoricalNavJobCron(),
                mfNavService::loadHistoricalDataIfNotExists);
        log.info("loadHistoricalNavJob scheduled successfully");

        log.info("Scheduling loadLastDayDataNav with cron: {}", schedulerProperties.getDailyDataJobCron());
        BackgroundJob.scheduleRecurrently(
                "daily-nav-load", schedulerProperties.getDailyDataJobCron(), mfNavService::loadLastDayDataNav);
        log.info("loadLastDayDataNav scheduled successfully");
    }

    private void scheduleAdaptiveStrategyJob() {
        log.info("Scheduling adaptive strategy job with cron: {}", schedulerProperties.getAdaptiveStrategyJobCron());
        BackgroundJob.scheduleRecurrently(
                "adaptive-cache-strategy",
                schedulerProperties.getAdaptiveStrategyJobCron(),
                adaptiveStrategyScheduler::adaptStrategy);
        log.info("Adaptive strategy job scheduled successfully");
    }
}
