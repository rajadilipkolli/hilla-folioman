package com.example.application.config;

import com.example.application.shared.UserSchemeDetailsService;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfiguration {

    private final UserSchemeDetailsService userSchemeDetailsService;

    public SchedulerConfiguration(UserSchemeDetailsService userSchemeDetailsService) {
        this.userSchemeDetailsService = userSchemeDetailsService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void setSchemeIfNotSetJob() {
        BackgroundJob.scheduleRecurrently(Cron.every5minutes(), userSchemeDetailsService::setAMFIIfNull);
    }
}
