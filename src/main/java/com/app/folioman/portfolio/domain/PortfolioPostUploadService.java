package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.shared.CacheNames;
import java.util.List;
import org.jobrunr.jobs.annotations.Job;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PortfolioPostUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioPostUploadService.class);
    private static final List<String> POST_UPLOAD_EVICT_CACHES = List.of(
            CacheNames.SUMMARY_CACHE,
            CacheNames.PORTFOLIO_HISTORY_CACHE,
            CacheNames.RETURNS_CACHE,
            CacheNames.TRANSACTION_CACHE,
            CacheNames.USER_PROFILE_CACHE,
            "emailAndName");

    private final UserSchemeDetailService userSchemeDetailService;
    private final PortfolioValueUpdateService portfolioValueUpdateService;
    private final @Nullable JdbcTemplate jdbcTemplate;
    private final @Nullable ObjectProvider<CacheManager> cacheManagerProvider;
    private final @Nullable TransactionTemplate transactionTemplate;

    PortfolioPostUploadService(
            UserSchemeDetailService userSchemeDetailService, PortfolioValueUpdateService portfolioValueUpdateService) {
        this(userSchemeDetailService, portfolioValueUpdateService, null, null, null);
    }

    @Autowired
    PortfolioPostUploadService(
            UserSchemeDetailService userSchemeDetailService,
            PortfolioValueUpdateService portfolioValueUpdateService,
            @Nullable JdbcTemplate jdbcTemplate,
            @Nullable ObjectProvider<CacheManager> cacheManagerProvider,
            @Nullable TransactionTemplate transactionTemplate) {
        this.userSchemeDetailService = userSchemeDetailService;
        this.portfolioValueUpdateService = portfolioValueUpdateService;
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManagerProvider = cacheManagerProvider;
        this.transactionTemplate = transactionTemplate;
    }

    @Job(name = "Process CAS Post Upload %0")
    public void processCasPostUpload(Long casId) {
        LOGGER.info("Executing durable post-upload job for CAS ID: {}", casId);
        if (transactionTemplate != null) {
            transactionTemplate.executeWithoutResult(status -> runLockedPostUpload(casId));
        } else {
            runLockedPostUpload(casId);
        }
        evictPostUploadCaches();
    }

    private void runLockedPostUpload(Long casId) {
        if (jdbcTemplate != null && TransactionSynchronizationManager.isSynchronizationActive()) {
            jdbcTemplate.queryForObject(
                    "SELECT pg_advisory_xact_lock(hashtext(?))", Object.class, "cas_post_upload:" + casId);
        }
        userSchemeDetailService.setUserSchemeAMFIIfNull(casId);
        portfolioValueUpdateService.updatePortfolioValueSync(casId);
    }

    private void evictPostUploadCaches() {
        CacheManager cacheManager = cacheManagerProvider != null ? cacheManagerProvider.getIfAvailable() : null;
        if (cacheManager == null) {
            return;
        }
        for (String cacheName : POST_UPLOAD_EVICT_CACHES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
