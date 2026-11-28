package com.app.folioman.config;

import com.app.folioman.config.redis.AdaptiveStrategyScheduler;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private final UserSchemeDetailService userSchemeDetailService;
    private final AdaptiveStrategyScheduler adaptiveStrategyScheduler;
    private final SchedulerProperties schedulerProperties;

    public SchedulerConfiguration(
            UserSchemeDetailService userSchemeDetailsService,
            AdaptiveStrategyScheduler adaptiveStrategyScheduler,
            SchedulerProperties schedulerProperties) {
        this.userSchemeDetailService = userSchemeDetailsService;
        this.adaptiveStrategyScheduler = adaptiveStrategyScheduler;
        this.schedulerProperties = schedulerProperties;
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleAllJobs(ApplicationStartedEvent event) {
        scheduleSetAMFIIfNullJob();
        scheduleAdaptiveStrategyJob();
        // Portfolio cache eviction job is scheduled separately in the portfolio module
    }

    private void scheduleSetAMFIIfNullJob() {
        LOGGER.info("Scheduling setAMFIIfNull job with cron: {}", schedulerProperties.getAmfiJobCron());
        BackgroundJob.scheduleRecurrently(
                "user-scheme-amfi-update",
                schedulerProperties.getAmfiJobCron(),
                () -> userSchemeDetailService.setUserSchemeAMFIIfNull());
        LOGGER.info("setAMFIIfNull job scheduled successfully");
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
