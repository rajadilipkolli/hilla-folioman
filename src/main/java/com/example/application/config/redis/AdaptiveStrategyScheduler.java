package com.example.application.config.redis;

import java.util.Map;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
class AdaptiveStrategyScheduler {

    private final CacheAdapter cacheAdapter;
    private final Monitor monitor;
    private final Evaluator evaluator;
    private final PolicyRepository policyRepository;

    AdaptiveStrategyScheduler(
            CacheAdapter cacheAdapter, Monitor monitor, Evaluator evaluator, PolicyRepository policyRepository) {
        this.cacheAdapter = cacheAdapter;
        this.monitor = monitor;
        this.evaluator = evaluator;
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    void adaptStrategy() {
        Map<String, Object> metrics = monitor.getMetrics();
        String newStrategy = evaluator.evaluate(metrics);
        CachePolicy newPolicy = policyRepository.getPolicy(newStrategy);
        cacheAdapter.setPolicy(newPolicy);
    }
}
