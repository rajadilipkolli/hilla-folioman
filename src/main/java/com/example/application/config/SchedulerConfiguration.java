package com.example.application.config;

import com.example.application.shared.UserSchemeDetailService;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.cron.Cron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private final UserSchemeDetailService userSchemeDetailService;

    public SchedulerConfiguration(UserSchemeDetailService userSchemeDetailsService) {
        this.userSchemeDetailService = userSchemeDetailsService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleSetAMFIIfNullJob() {
        log.info("Scheduling setAMFIIfNull job to run every 5 minutes");
        BackgroundJob.scheduleRecurrently(Cron.every5minutes(), userSchemeDetailService::setUserSchemeAMFIIfNull);
        log.info("setAMFIIfNull job scheduled successfully");
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleLoadHistoricalNavJob() {
        log.info("Scheduling loadHistoricalNavJob to run every 5 minutes");
        BackgroundJob.scheduleRecurrently(Cron.every5minutes(), userSchemeDetailService::loadHistoricalDataIfNotExists);
        log.info("loadHistoricalNavJob scheduled successfully");
    }
}
