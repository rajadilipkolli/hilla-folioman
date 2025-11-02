package com.app.folioman.config.redis;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptive strategy scheduler for Redis cache management.
 * This class evaluates metrics and adapts cache strategies based on usage patterns.
 *
 * Note: Scheduling is now managed centrally via JobRunr in SchedulerConfiguration.
 */
@Component
public class AdaptiveStrategyScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveStrategyScheduler.class);

    private final CacheAdapter cacheAdapter;
    private final Monitor monitor;
    private final Evaluator evaluator;
    private final PolicyRepository policyRepository;

    // Track the last applied strategy to avoid unnecessary changes
    private String lastAppliedStrategy = null;

    // Counter to track consecutive strategy matches
    private int consecutiveStrategyMatches = 0;

    // Configurable interval for strategy evaluation (default 10 minutes)
    @Value("${app.cache.adaptive-strategy.interval-ms:600000}")
    private long adaptiveStrategyIntervalMs;

    // Configurable threshold for strategy stability (default 3)
    @Value("${app.cache.adaptive-strategy.stability-threshold:3}")
    private int stabilityThreshold;

    public AdaptiveStrategyScheduler(
            CacheAdapter cacheAdapter, Monitor monitor, Evaluator evaluator, PolicyRepository policyRepository) {
        this.cacheAdapter = cacheAdapter;
        this.monitor = monitor;
        this.evaluator = evaluator;
        this.policyRepository = policyRepository;
    }

    /**
     * Evaluates current cache metrics and adapts the caching strategy if necessary.
     * This method is called by JobRunr based on schedule configured in SchedulerConfiguration.
     */
    public void adaptStrategy() {
        try {
            Map<String, Object> metrics = monitor.getMetrics();

            if (metrics == null) {
                log.debug("Cache metrics are null, skipping evaluation");
                return;
            }

            // only evaluate when metrics is non-null
            String newStrategy = evaluator.evaluate(metrics);

            log.debug(
                    "Cache metrics - Size: {}, Hit Rate: {}, Memory Usage: {}",
                    metrics.get("cacheSize"),
                    metrics.get("hitRate"),
                    metrics.get("memoryUsage"));

            // Implement circuit breaker pattern to avoid excessive changes
            if (lastAppliedStrategy != null && lastAppliedStrategy.equals(newStrategy)) {
                consecutiveStrategyMatches++;
                log.debug("Same strategy detected {} consecutive times: {}", consecutiveStrategyMatches, newStrategy);

                // If stable, reduce frequency of actual policy changes
                if (consecutiveStrategyMatches < stabilityThreshold) {
                    log.debug("Skipping policy application until stability threshold reached");
                    return;
                }
            } else {
                // Reset counter when strategy changes
                consecutiveStrategyMatches = 0;
            }

            // Only apply if strategy has changed, or we've confirmed it's stable
            if (lastAppliedStrategy == null
                    || !lastAppliedStrategy.equals(newStrategy)
                    || consecutiveStrategyMatches >= stabilityThreshold) {
                log.info("Applying new cache strategy: {}", newStrategy);
                CachePolicy newPolicy = policyRepository.getPolicy(newStrategy);
                cacheAdapter.setPolicy(newPolicy);
                lastAppliedStrategy = newStrategy;
            }
        } catch (Exception e) {
            log.error("Error adapting cache strategy", e);
        }
    }
}
