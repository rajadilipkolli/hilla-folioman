package com.app.folioman.mfschemes.config;

import com.app.folioman.mfschemes.domain.MfSchemeSyncService;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
class MfSchemesSchedulerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemesSchedulerConfiguration.class);

    private final String schemeSyncJobCron;
    private final MfSchemeSyncService mfSchemeSyncService;

    MfSchemesSchedulerConfiguration(
            @Value("${app.scheduler.scheme-sync-job-cron:0 0 20 * * SUN}") String schemeSyncJobCron,
            MfSchemeSyncService mfSchemeSyncService) {
        this.schemeSyncJobCron = schemeSyncJobCron;
        this.mfSchemeSyncService = mfSchemeSyncService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleJobs(ApplicationStartedEvent event) {
        LOGGER.info("Scheduling scheme sync job with cron: {}", schemeSyncJobCron);
        BackgroundJob.scheduleRecurrently("update-mf-schemes", schemeSyncJobCron, mfSchemeSyncService::syncAllSchemes);
        LOGGER.info("scheme sync job scheduled successfully");
    }
}
