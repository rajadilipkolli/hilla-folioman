package com.app.folioman.config.redis;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReduceCacheSizePolicy implements CachePolicy {

    private static final double REDUCTION_PERCENTAGE = 0.3; // Reduce by 30% instead of 50%
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(30); // Default expiration time
    private static final Logger log = LoggerFactory.getLogger(ReduceCacheSizePolicy.class);
    private static final String PROTECTED_KEY_PREFIX = "critical:"; // Keys that should never be evicted

    @Override
    public Duration getExpirationTime() {
        return EXPIRATION_TIME;
    }

    @Override
    public void apply(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        // Use the more efficient scan operation from Monitor
        Monitor monitor = new Monitor(redisTemplate, meterRegistry);
        Set<String> allKeys = monitor.scanKeys("*");

        if (allKeys == null || allKeys.isEmpty()) {
            log.info("Cache is empty, nothing to reduce.");
            return;
        }

        // Calculate how many keys to remove
        int totalKeys = allKeys.size();
        int keysToRemove = (int) (totalKeys * REDUCTION_PERCENTAGE);

        log.info("Reducing cache size by removing up to {} entries using LRU strategy.", keysToRemove);

        // Track access counts for keys to implement LRU-like eviction
        List<KeyMetadata> keyMetadataList = getKeyMetadataList(allKeys, meterRegistry, redisTemplate);

        // Sort keys by access frequency (least used first) and then by TTL (expiring soon first)
        int removedCount = evictLeastUsedKeys(keyMetadataList, keysToRemove, redisTemplate);

        log.info("Cache reduction complete. Removed {} entries.", removedCount);

        // Apply shorter TTL to remaining keys that had low access counts but weren't evicted
        applyDynamicTTL(keyMetadataList, removedCount, redisTemplate);
    }

    private List<KeyMetadata> getKeyMetadataList(
            Set<String> allKeys, MeterRegistry meterRegistry, RedisTemplate<String, Object> redisTemplate) {
        List<KeyMetadata> keyMetadataList = new ArrayList<>(allKeys.size());

        for (String key : allKeys) {
            // Skip protected keys
            if (key.startsWith(PROTECTED_KEY_PREFIX)) {
                continue;
            }

            // Get access count from metrics
            double accessCount = getAccessCountForKey(meterRegistry, key);

            // Get remaining TTL
            Long ttl = redisTemplate.getExpire(key);
            long remainingTtl = (ttl != null && ttl > 0) ? ttl : 0;

            keyMetadataList.add(new KeyMetadata(key, accessCount, remainingTtl));
        }

        return keyMetadataList;
    }

    private int evictLeastUsedKeys(
            List<KeyMetadata> keyMetadataList, int keysToRemove, RedisTemplate<String, Object> redisTemplate) {
        // Sort by access count (ascending) and then by TTL (ascending)
        keyMetadataList.sort(
                Comparator.comparingDouble(KeyMetadata::accessCount).thenComparingLong(KeyMetadata::remainingTtl));

        int removedCount = 0;

        // Remove the least accessed keys
        for (KeyMetadata metadata : keyMetadataList) {
            if (removedCount >= keysToRemove) break;

            redisTemplate.delete(metadata.key());
            removedCount++;

            if (removedCount % 100 == 0) {
                log.debug("Removed {} keys so far", removedCount);
            }
        }

        return removedCount;
    }

    private void applyDynamicTTL(
            List<KeyMetadata> keyMetadataList, int startIndex, RedisTemplate<String, Object> redisTemplate) {
        // Apply shorter TTL to keys with low access counts that weren't evicted
        int adjustedCount = 0;
        Duration shorterTTL = Duration.ofMinutes(15); // Shorter TTL for less-used keys

        for (int i = startIndex; i < Math.min(startIndex + 100, keyMetadataList.size()); i++) {
            KeyMetadata metadata = keyMetadataList.get(i);
            // If access count is low but key wasn't evicted, reduce its TTL
            if (metadata.accessCount() < 5) {
                redisTemplate.expire(metadata.key(), shorterTTL);
                adjustedCount++;
            }
        }

        if (adjustedCount > 0) {
            log.info("Applied shorter TTL to {} additional low-usage keys", adjustedCount);
        }
    }

    private double getAccessCountForKey(MeterRegistry meterRegistry, String key) {
        if (key.indexOf("SimpleKey") > 0) {
            key = key.substring(key.indexOf("SimpleKey"));
        } else if (key.indexOf("::") > 0) {
            key = key.substring(key.indexOf("::") + 2);
        }
        return meterRegistry.counter("cache.access", "key", key).count();
    }

    // Record to store key metadata for eviction decisions
    private record KeyMetadata(String key, double accessCount, long remainingTtl) {}
}
