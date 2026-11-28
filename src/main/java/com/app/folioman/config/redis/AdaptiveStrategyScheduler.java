package com.app.folioman.config.redis;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptive strategy scheduler for Redis cache management.
 * This class evaluates metrics and adapts cache strategies based on usage patterns.
 *
 * Note: Scheduling is now managed centrally via JobRunr in SchedulerConfiguration.
 * Strategy state is coordinated via Redis when a StringRedisTemplate is available so
 * all pods executing the recurring job see consistent state.
 */
@Component
public class AdaptiveStrategyScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptiveStrategyScheduler.class);
    static final String LAST_STRATEGY_KEY = "folioman:adaptive:last_strategy";
    static final String CONSECUTIVE_MATCHES_KEY = "folioman:adaptive:consecutive_matches";

    private final CacheAdapter cacheAdapter;
    private final Monitor monitor;
    private final Evaluator evaluator;
    private final PolicyRepository policyRepository;
    private final RedisAppProperties redisAppProperties;
    private final @Nullable StringRedisTemplate stringRedisTemplate;

    // Fallback local state when Redis coordination is unavailable (e.g. unit tests)
    private @Nullable String lastAppliedStrategy = null;

    // Counter to track consecutive strategy matches
    private int consecutiveStrategyMatches = 0;

    public AdaptiveStrategyScheduler(
            CacheAdapter cacheAdapter,
            Monitor monitor,
            Evaluator evaluator,
            PolicyRepository policyRepository,
            RedisAppProperties redisAppProperties) {
        this(cacheAdapter, monitor, evaluator, policyRepository, redisAppProperties, null);
    }

    @Autowired
    public AdaptiveStrategyScheduler(
            CacheAdapter cacheAdapter,
            Monitor monitor,
            Evaluator evaluator,
            PolicyRepository policyRepository,
            RedisAppProperties redisAppProperties,
            @Nullable StringRedisTemplate stringRedisTemplate) {
        this.cacheAdapter = cacheAdapter;
        this.monitor = monitor;
        this.evaluator = evaluator;
        this.policyRepository = policyRepository;
        this.redisAppProperties = redisAppProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Evaluates current cache metrics and adapts the caching strategy if necessary.
     * This method is called by JobRunr based on schedule configured in SchedulerConfiguration.
     */
    public void adaptStrategy() {
        try {
            Map<String, Object> metrics = monitor.getMetrics();

            if (metrics == null) {
                LOGGER.debug("Cache metrics are null, skipping evaluation");
                return;
            }

            // only evaluate when metrics is non-null
            String newStrategy = evaluator.evaluate(metrics);

            LOGGER.debug(
                    "Cache metrics - Size: {}, Hit Rate: {}, Memory Usage: {}",
                    metrics.get("cacheSize"),
                    metrics.get("hitRate"),
                    metrics.get("memoryUsage"));

            String currentLastStrategy = getLastAppliedStrategy();
            int currentConsecutiveMatches = getConsecutiveStrategyMatches();

            // Implement circuit breaker pattern to avoid excessive changes
            if (currentLastStrategy != null && currentLastStrategy.equals(newStrategy)) {
                currentConsecutiveMatches++;
                saveConsecutiveStrategyMatches(currentConsecutiveMatches);
                LOGGER.debug("Same strategy detected {} consecutive times: {}", currentConsecutiveMatches, newStrategy);

                // If stable, reduce frequency of actual policy changes
                if (currentConsecutiveMatches
                        < redisAppProperties.getAdaptiveStrategy().getStabilityThreshold()) {
                    LOGGER.debug("Skipping policy application until stability threshold reached");
                    return;
                }
            } else {
                // Reset counter when strategy changes
                currentConsecutiveMatches = 0;
                saveConsecutiveStrategyMatches(0);
            }

            // Only apply if strategy has changed, or we've confirmed it's stable
            if (currentLastStrategy == null
                    || !currentLastStrategy.equals(newStrategy)
                    || currentConsecutiveMatches
                            >= redisAppProperties.getAdaptiveStrategy().getStabilityThreshold()) {
                LOGGER.info("Applying new cache strategy: {}", newStrategy);
                CachePolicy newPolicy = policyRepository.getPolicy(newStrategy);
                cacheAdapter.setPolicy(newPolicy);
                saveLastAppliedStrategy(newStrategy);
            }
        } catch (Exception e) {
            LOGGER.error("Error adapting cache strategy", e);
        }
    }

    private @Nullable String getLastAppliedStrategy() {
        if (stringRedisTemplate != null) {
            try {
                String value = stringRedisTemplate.opsForValue().get(LAST_STRATEGY_KEY);
                if (value != null) {
                    lastAppliedStrategy = value;
                    return value;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read last applied strategy from Redis, using local state", e);
            }
        }
        return lastAppliedStrategy;
    }

    private void saveLastAppliedStrategy(String strategy) {
        lastAppliedStrategy = strategy;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(LAST_STRATEGY_KEY, strategy);
            } catch (Exception e) {
                LOGGER.warn("Failed to persist last applied strategy to Redis", e);
            }
        }
    }

    private int getConsecutiveStrategyMatches() {
        if (stringRedisTemplate != null) {
            try {
                String value = stringRedisTemplate.opsForValue().get(CONSECUTIVE_MATCHES_KEY);
                if (value != null) {
                    int parsed = Integer.parseInt(value);
                    consecutiveStrategyMatches = parsed;
                    return parsed;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read consecutive strategy matches from Redis, using local state", e);
            }
        }
        return consecutiveStrategyMatches;
    }

    private void saveConsecutiveStrategyMatches(int matches) {
        consecutiveStrategyMatches = matches;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(CONSECUTIVE_MATCHES_KEY, Integer.toString(matches));
            } catch (Exception e) {
                LOGGER.warn("Failed to persist consecutive strategy matches to Redis", e);
            }
        }
    }
}
