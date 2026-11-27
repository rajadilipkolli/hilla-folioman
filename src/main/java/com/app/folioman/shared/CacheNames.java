package com.app.folioman.shared;

/**
 * Centralized definition of cache names used across the application.
 * This helps maintain consistency and avoid naming conflicts between modules.
 */
public final class CacheNames {

    // MF Schemes module caches
    public static final String SCHEME_SEARCH_CACHE = "schemeSearchCache";

    // Auth module caches
    public static final String USER_PROFILE_CACHE = "userProfileCache";

    // Portfolio module caches
    public static final String TRANSACTION_CACHE = "transactionCache";
    public static final String RETURNS_CACHE = "returnsCache";
    public static final String PORTFOLIO_HISTORY_CACHE = "portfolioHistoryCache";
    public static final String SUMMARY_CACHE = "portfolioSummaryCache";
    public static final String EMAIL_AND_NAME_CACHE = "emailAndName";

    // Prevent instantiation
    private CacheNames() {
        throw new AssertionError("Utility class, do not instantiate");
    }
}
