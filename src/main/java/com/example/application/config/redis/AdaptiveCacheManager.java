package com.example.application.config.redis;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AdaptiveCacheManager {

    private final CacheAdapter cacheAdapter;
    private final Monitor monitor;
    private final Evaluator evaluator;
    private final PolicyRepository policyRepository;

    @Autowired
    public AdaptiveCacheManager(
            CacheAdapter cacheAdapter, Monitor monitor, Evaluator evaluator, PolicyRepository policyRepository) {
        this.cacheAdapter = cacheAdapter;
        this.monitor = monitor;
        this.evaluator = evaluator;
        this.policyRepository = policyRepository;
    }

    public <T> T get(String key, Class<T> type) {
        T value = cacheAdapter.get(key, type);
        monitor.recordAccess(key);
        return value;
    }

    public <T> void put(String key, T value) {
        cacheAdapter.put(key, value);
        monitor.recordUpdate(key);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void adaptStrategy() {
        Map<String, Object> metrics = monitor.getMetrics();
        String newStrategy = evaluator.evaluate(metrics);
        CachePolicy newPolicy = policyRepository.getPolicy(newStrategy);
        cacheAdapter.setPolicy(newPolicy);
    }
}
