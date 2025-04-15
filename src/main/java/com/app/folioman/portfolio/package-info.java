/**
 * The Portfolio module manages user investment portfolios, transactions, and portfolio analysis.
 *
 * This module:
 * - Manages user portfolio data and transactions
 * - Provides portfolio valuation and performance analysis
 * - Handles rebalancing recommendations
 * - Calculates returns and other investment metrics
 *
 * This module depends on the MF Schemes module for mutual fund data.
 */
@ApplicationModule(
        displayName = "Portfolio Management",
        allowedDependencies = {"mfschemes", "shared"})
package com.app.folioman.portfolio;

import org.springframework.modulith.ApplicationModule;
