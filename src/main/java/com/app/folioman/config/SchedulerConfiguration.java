package com.app.folioman.config;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.portfolio.UserSchemeDetailService;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.cron.Cron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private final UserSchemeDetailService userSchemeDetailService;
    private final MFNavService mfNavService;

    SchedulerConfiguration(UserSchemeDetailService userSchemeDetailsService, MFNavService mfNavService) {
        this.userSchemeDetailService = userSchemeDetailsService;
        this.mfNavService = mfNavService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleSetAMFIIfNullJob() {
        log.info("Scheduling setAMFIIfNull job to run every 5 minutes");
        BackgroundJob.scheduleRecurrently(Cron.every5minutes(), userSchemeDetailService::setUserSchemeAMFIIfNull);
        log.info("setAMFIIfNull job scheduled successfully");
    }

    @EventListener(ApplicationStartedEvent.class)
    void scheduleLoadHistoricalNavJob() {
        log.info("Scheduling loadHistoricalNavJob to run every 30 minutes");
        BackgroundJob.scheduleRecurrently(Cron.everyHalfHour(), mfNavService::loadHistoricalDataIfNotExists);
        log.info("loadHistoricalNavJob scheduled successfully");
        log.info("Scheduling loadLastDayDataNav to run Daily");
        BackgroundJob.scheduleRecurrently(Cron.daily(), mfNavService::loadLastDayDataNav);
        log.info("loadLastDayDataNav scheduled successfully");
    }
}
