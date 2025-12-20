package com.app.folioman.config.redis;

/**
 * Centralized definition of cache names used across the application.
 * This helps maintain consistency and avoid naming conflicts between modules.
 */
public final class CacheNames {

    // MF Schemes module caches
    public static final String SCHEME_SEARCH_CACHE = "schemeSearchCache";

    // Portfolio module caches
    public static final String TRANSACTION_CACHE = "transactionCache";

    // Prevent instantiation
    private CacheNames() {
        throw new AssertionError("Utility class, do not instantiate");
    }
}
