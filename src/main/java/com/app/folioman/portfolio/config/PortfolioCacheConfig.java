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
import org.springframework.util.StopWatch;

/**
 * Portfolio module-specific cache configuration.
 * Handles the scheduled eviction of transaction-related caches.
 * Implements an adaptive eviction strategy based on cache size and access patterns.
 */
@Configuration
@EnableCaching
public class PortfolioCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(PortfolioCacheConfig.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final PortfolioCacheProperties portfolioCacheProperties;

    public PortfolioCacheConfig(
            RedisTemplate<String, Object> redisTemplate, PortfolioCacheProperties portfolioCacheProperties) {
        this.redisTemplate = redisTemplate;
        this.portfolioCacheProperties = portfolioCacheProperties;
    }

    /**
     * Schedules the transaction cache eviction job using the configured cron expression.
     * The job is scheduled using JobRunr which provides persistence and monitoring.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void scheduleTransactionCacheEvictionJob(ApplicationStartedEvent event) {
        log.info(
                "Scheduling transaction cache eviction job with cron: {}",
                portfolioCacheProperties.getEviction().getTransactionCron());

        // Schedule the primary daily eviction job
        BackgroundJob.scheduleRecurrently(
                "transaction-cache-eviction",
                portfolioCacheProperties.getEviction().getTransactionCron(),
                this::evictTransactionCaches);

        log.info("Transaction cache eviction jobs scheduled successfully");
    }

    /**
     * Main cache eviction job that runs on the scheduled cron expression.
     * Performs a thorough eviction of transaction caches that match certain patterns.
     */
    public void evictTransactionCaches() {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("Transaction Cache Eviction");
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            log.info("Executing scheduled transaction cache eviction at {}", now);

            // Evict all entries with "monthly_" and "yearly_" patterns in the TRANSACTION_CACHE
            String cacheKeyPattern = CacheNames.TRANSACTION_CACHE + "::*";

            // Find all keys matching our pattern
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);

            if (keys.isEmpty()) {
                log.info("No transaction cache entries found to evict");
                return;
            }

            log.info("Found {} transaction cache entries to check for eviction", keys.size());

            int evictedCount = 0;
            int batchCount = 0;

            for (String key : keys) {
                if (key.contains("monthly_") || key.contains("yearly_")) {
                    redisTemplate.delete(key);
                    evictedCount++;
                    batchCount++;

                    // Process in batches to avoid Redis blocking for too long
                    if (batchCount >= portfolioCacheProperties.getEviction().getBatchSize()) {
                        log.debug("Evicted {} cache entries so far", evictedCount);
                        batchCount = 0;
                        // Small delay between batches to reduce Redis load
                        Thread.sleep(10);
                    }
                }
            }

            stopWatch.stop();
            log.info(
                    "Successfully evicted {} transaction cache entries in {} ms",
                    evictedCount,
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            log.error("Error during transaction cache eviction", e);
        }
    }
}
