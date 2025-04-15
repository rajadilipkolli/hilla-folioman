package com.app.folioman.portfolio.config;

import com.app.folioman.config.redis.CacheNames;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Portfolio module-specific cache configuration.
 * Handles the scheduled eviction of transaction-related caches.
 */
@Configuration
@EnableCaching
public class PortfolioCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(PortfolioCacheConfig.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public PortfolioCacheConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Schedules the transaction cache eviction job to run at 12:15 AM IST daily.
     * The job is scheduled using JobRunr which provides persistence and monitoring.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void scheduleTransactionCacheEvictionJob(ApplicationStartedEvent event) {
        log.info("Scheduling transaction cache eviction job to run at 12:15 AM IST daily");
        // Using custom cron expression for 12:15 AM IST (equivalent to "0 15 0 * * *" in Asia/Kolkata timezone)
        // Note: JobRunr cron expressions use UTC by default, so we adjust for IST (UTC+5:30)
        BackgroundJob.scheduleRecurrently("transaction-cache-eviction", "0 45 18 * * *", this::evictTransactionCaches);
        log.info("Transaction cache eviction job scheduled successfully");
    }

    /**
     * Job to evict all transaction-related caches.
     * Will be executed at 12:15 AM IST (6:45 PM UTC) daily.
     *
     * Note: The cron pattern is "second minute hour day-of-month month day-of-week"
     * IST is UTC+5:30, so 12:15 AM IST = 18:45 PM UTC (previous day)
     */
    public void evictTransactionCaches() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        log.info("Executing scheduled transaction cache eviction at {}", now);

        // Evict all entries with "monthly_" and "yearly_" patterns in the TRANSACTION_CACHE
        String cacheKeyPattern = CacheNames.TRANSACTION_CACHE + "::*";

        // Find all keys matching our pattern
        Set<String> keys = redisTemplate.keys(cacheKeyPattern);
        if (!keys.isEmpty()) {
            log.info("Found {} transaction cache entries to evict", keys.size());

            for (String key : keys) {
                if (key.contains("monthly_") || key.contains("yearly_")) {
                    redisTemplate.delete(key);
                    log.debug("Evicted cache entry: {}", key);
                }
            }

            log.info("Successfully evicted {} transaction cache entries", keys.size());
        } else {
            log.info("No transaction cache entries found to evict");
        }
    }
}
