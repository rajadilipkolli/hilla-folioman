package com.app.folioman.portfolio.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Utility class for financial calculations related to investments.
 */
public class XirrCalculator {

    private static final double DAYS_IN_YEAR = 365.0;
    private static final int MAX_ITERATIONS = 100;
    private static final double PRECISION = 0.000001;

    /**
     * Calculate XIRR (Extended Internal Rate of Return) for a series of cash flows.
     *
     * @param cashFlows List of cash flow amounts (negative for outflows, positive for inflows)
     * @param dates List of dates corresponding to each cash flow
     * @return The calculated XIRR as a decimal (e.g. 0.08 for 8%)
     * @throws IllegalArgumentException if the inputs are invalid or calculation cannot converge
     */
    public static BigDecimal calculateXirr(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        if (cashFlows == null
                || dates == null
                || cashFlows.isEmpty()
                || dates.isEmpty()
                || cashFlows.size() != dates.size()) {
            throw new IllegalArgumentException("Invalid cash flows or dates provided");
        }

        // Check for valid cash flow pattern (at least one positive and one negative)
        boolean hasPositive = false;
        boolean hasNegative = false;
        for (BigDecimal amount : cashFlows) {
            if (amount == null) {
                throw new IllegalArgumentException("Cash flow amounts cannot be null");
            }
            if (amount.compareTo(BigDecimal.ZERO) > 0) hasPositive = true;
            if (amount.compareTo(BigDecimal.ZERO) < 0) hasNegative = true;
        }

        if (!hasPositive || !hasNegative) {
            throw new IllegalArgumentException("Cash flows must have at least one positive and one negative value");
        }

        // Calculate XIRR using Newton-Raphson method with better initial guess
        double guess = estimateInitialGuess(cashFlows, dates);
        double previousGuess;
        int iteration = 0;

        while (iteration < MAX_ITERATIONS) {
            double xirrValue = computeXirrFunction(cashFlows, dates, guess);
            double xirrDerivative = computeXirrDerivative(cashFlows, dates, guess);

            // Handle small derivative with alternative approach
            if (Math.abs(xirrDerivative) < PRECISION) {
                // Try bisection method as fallback
                return calculateXirrWithBisection(cashFlows, dates);
            }

            previousGuess = guess;
            guess = previousGuess - xirrValue / xirrDerivative;

            // Check for convergence
            if (Math.abs(guess - previousGuess) < PRECISION) {
                // Return the calculated XIRR as a BigDecimal with appropriate precision
                return BigDecimal.valueOf(guess);
            }

            iteration++;
        }

        throw new IllegalArgumentException("XIRR calculation did not converge after " + MAX_ITERATIONS + " iterations");
    }

    private static double estimateInitialGuess(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        // Simple estimation based on total return
        double totalInflow = 0;
        double totalOutflow = 0;

        for (BigDecimal flow : cashFlows) {
            if (flow.compareTo(BigDecimal.ZERO) > 0) {
                totalInflow += flow.doubleValue();
            } else {
                totalOutflow += Math.abs(flow.doubleValue());
            }
        }

        // Start with either 10% or a simple return-based guess
        if (totalOutflow == 0) return 0.1;

        double simpleReturn = (totalInflow / totalOutflow) - 1;
        // Limit the initial guess to a reasonable range
        return Math.max(-0.9, Math.min(simpleReturn, 0.9));
    }

    private static BigDecimal calculateXirrWithBisection(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        // Bisection method is more robust but slower
        double left = -0.999;  // -99.9%
        double right = 10.0;   // 1000%
        double mid;

        for (int i = 0; i < MAX_ITERATIONS * 2; i++) {
            mid = (left + right) / 2;
            double value = computeXirrFunction(cashFlows, dates, mid);

            if (Math.abs(value) < PRECISION) {
                return BigDecimal.valueOf(mid);
            }

            if (value > 0) {
                left = mid;
            } else {
                right = mid;
            }

            if (Math.abs(right - left) < PRECISION) {
                return BigDecimal.valueOf(mid);
            }
        }

        throw new IllegalArgumentException("XIRR calculation did not converge using bisection method");
    }

    private static double computeXirrFunction(List<BigDecimal> cashFlows, List<LocalDate> dates, double rate) {
        double result = 0;
        LocalDate initialDate = dates.getFirst();

        for (int i = 0; i < cashFlows.size(); i++) {
            double daysFromStart = ChronoUnit.DAYS.between(initialDate, dates.get(i));
            double exponent = daysFromStart / DAYS_IN_YEAR;
            result += cashFlows.get(i).doubleValue() / Math.pow(1.0 + rate, exponent);
        }

        return result;
    }

    private static double computeXirrDerivative(List<BigDecimal> cashFlows, List<LocalDate> dates, double rate) {
        double result = 0;
        LocalDate initialDate = dates.getFirst();

        for (int i = 0; i < cashFlows.size(); i++) {
            double daysFromStart = ChronoUnit.DAYS.between(initialDate, dates.get(i));
            double exponent = daysFromStart / DAYS_IN_YEAR;
            result -= exponent * cashFlows.get(i).doubleValue() / Math.pow(1.0 + rate, exponent + 1);
        }

        return result;
    }
}
