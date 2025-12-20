/**
 * The Mutual Fund Schemes module handles all operations related to mutual fund schemes,
 * including scheme listing, search, NAV updates, and AMC management.
 *
 * This module:
 * - Provides scheme search and lookup functionality
 * - Manages NAV data and updates
 * - Handles AMC (Asset Management Company) data
 * - Offers API endpoints for scheme operations
 *
 * This module is allowed to be used by the Portfolio module.
 */
@ApplicationModule(
        displayName = "Mutual Fund Schemes",
        allowedDependencies = {"shared"})
package com.app.folioman.mfschemes;

import org.springframework.modulith.ApplicationModule;
