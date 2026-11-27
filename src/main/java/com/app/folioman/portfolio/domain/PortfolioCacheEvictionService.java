package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.CacheNames;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Service for evicting portfolio-related caches after background jobs complete.
 */
@Service
public class PortfolioCacheEvictionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioCacheEvictionService.class);

    private static final List<String> PORTFOLIO_CACHES = List.of(
            CacheNames.SUMMARY_CACHE,
            CacheNames.PORTFOLIO_HISTORY_CACHE,
            CacheNames.RETURNS_CACHE,
            CacheNames.TRANSACTION_CACHE,
            CacheNames.USER_PROFILE_CACHE,
            CacheNames.EMAIL_AND_NAME_CACHE);

    private final CacheManager cacheManager;

    PortfolioCacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Clears all portfolio-related caches so newly computed valuations are immediately visible.
     */
    public void evictPortfolioCaches() {
        for (String cacheName : PORTFOLIO_CACHES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        LOGGER.debug("Evicted portfolio caches: {}", PORTFOLIO_CACHES);
    }
}
