package com.app.folioman.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for all scheduled jobs in the application.
 */
@Configuration
@ConfigurationProperties(prefix = "app.scheduler")
public class SchedulerProperties {

    /**
     * Cron expression for the AMFI job (set AMFI if null)
     * Default: every 5 minutes
     */
    private String amfiJobCron = "*/5 * * * *";

    /**
     * Cron expression for historical NAV data loading job
     * Default: every 30 minutes
     */
    private String historicalNavJobCron = "*/30 * * * *";

    /**
     * Cron expression for daily NAV data loading job
     * Default: midnight every day
     */
    private String dailyDataJobCron = "0 0 0 * * *";

    /**
     * Cron expression for adaptive strategy evaluation job
     * Default: every 10 minutes
     */
    private String adaptiveStrategyJobCron = "*/10 * * * *";

    /**
     * Cron expression for transaction cache eviction job (UTC)
     * Default: 18:45 UTC daily (00:15 IST)
     */
    private String transactionCacheEvictionCron = "0 45 18 * * *";

    // Getters and setters
    public String getAmfiJobCron() {
        return amfiJobCron;
    }

    public void setAmfiJobCron(String amfiJobCron) {
        this.amfiJobCron = amfiJobCron;
    }

    public String getHistoricalNavJobCron() {
        return historicalNavJobCron;
    }

    public void setHistoricalNavJobCron(String historicalNavJobCron) {
        this.historicalNavJobCron = historicalNavJobCron;
    }

    public String getDailyDataJobCron() {
        return dailyDataJobCron;
    }

    public void setDailyDataJobCron(String dailyDataJobCron) {
        this.dailyDataJobCron = dailyDataJobCron;
    }

    public String getAdaptiveStrategyJobCron() {
        return adaptiveStrategyJobCron;
    }

    public void setAdaptiveStrategyJobCron(String adaptiveStrategyJobCron) {
        this.adaptiveStrategyJobCron = adaptiveStrategyJobCron;
    }

    public String getTransactionCacheEvictionCron() {
        return transactionCacheEvictionCron;
    }

    public void setTransactionCacheEvictionCron(String transactionCacheEvictionCron) {
        this.transactionCacheEvictionCron = transactionCacheEvictionCron;
    }
}
