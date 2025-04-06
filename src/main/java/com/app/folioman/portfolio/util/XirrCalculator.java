package com.app.folioman.portfolio.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Utility class for financial calculations related to investments.
 */
public class XirrCalculator {

    // Constants as BigDecimal for improved precision
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365.2425");
    private static final int MAX_ITERATIONS = 100;
    private static final BigDecimal PRECISION = new BigDecimal("0.000001");
    private static final int CALCULATION_SCALE = 16;
    private static final MathContext MC = new MathContext(CALCULATION_SCALE, RoundingMode.HALF_EVEN);

    /**
     * Calculate XIRR (Extended Internal Rate of Return) for a series of cash flows.
     *
     * @param cashFlows List of cash flow amounts (negative for outflows, positive for inflows)
     * @param dates List of dates corresponding to each cash flow
     * @return The calculated XIRR as a decimal (e.g. 0.08 for 8%)
     * @throws IllegalArgumentException if the inputs are invalid or calculation cannot converge
     */
    public static BigDecimal calculateXirr(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        validateInputs(cashFlows, dates);

        // Calculate XIRR using Newton-Raphson method with better initial guess
        BigDecimal guess = estimateInitialGuess(cashFlows, dates);
        BigDecimal previousGuess;
        int iteration = 0;

        while (iteration < MAX_ITERATIONS) {
            BigDecimal xirrValue = computeXirrFunction(cashFlows, dates, guess);
            BigDecimal xirrDerivative = computeXirrDerivative(cashFlows, dates, guess);

            // Handle small derivative with alternative approach
            if (xirrDerivative.abs().compareTo(PRECISION) < 0) {
                // Try bisection method as fallback
                return calculateXirrWithBisection(cashFlows, dates);
            }

            previousGuess = guess;
            guess = previousGuess.subtract(xirrValue.divide(xirrDerivative, CALCULATION_SCALE, RoundingMode.HALF_EVEN));

            // Check for convergence
            if (guess.subtract(previousGuess).abs().compareTo(PRECISION) < 0) {
                // Return the calculated XIRR as a BigDecimal with appropriate precision
                return guess.setScale(8, RoundingMode.HALF_EVEN);
            }

            iteration++;
        }

        throw new IllegalArgumentException("XIRR calculation did not converge after " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Validates the input parameters for XIRR calculation
     *
     * @param cashFlows List of cash flow amounts
     * @param dates List of dates for each cash flow
     * @throws IllegalArgumentException if inputs are invalid
     */
    private static void validateInputs(List<BigDecimal> cashFlows, List<LocalDate> dates) {
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
    }

    private static BigDecimal estimateInitialGuess(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        // Simple estimation based on total return
        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;

        for (BigDecimal flow : cashFlows) {
            if (flow.compareTo(BigDecimal.ZERO) > 0) {
                totalInflow = totalInflow.add(flow);
            } else {
                totalOutflow = totalOutflow.add(flow.abs());
            }
        }

        // Start with either 10% or a simple return-based guess
        if (totalOutflow.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("0.1");
        }

        BigDecimal simpleReturn = totalInflow.divide(totalOutflow, MC).subtract(BigDecimal.ONE);

        // Limit the initial guess to a reasonable range
        BigDecimal minGuess = new BigDecimal("-0.9");
        BigDecimal maxGuess = new BigDecimal("0.9");

        if (simpleReturn.compareTo(minGuess) < 0) {
            return minGuess;
        } else if (simpleReturn.compareTo(maxGuess) > 0) {
            return maxGuess;
        } else {
            return simpleReturn;
        }
    }

    private static BigDecimal calculateXirrWithBisection(List<BigDecimal> cashFlows, List<LocalDate> dates) {
        // Bisection method is more robust but slower
        BigDecimal left = new BigDecimal("-0.999"); // -99.9%
        BigDecimal right = new BigDecimal("10.0"); // 1000%
        BigDecimal mid;

        for (int i = 0; i < MAX_ITERATIONS * 2; i++) {
            mid = left.add(right).divide(new BigDecimal("2"), MC);
            BigDecimal value = computeXirrFunction(cashFlows, dates, mid);

            if (value.abs().compareTo(PRECISION) < 0) {
                return mid.setScale(8, RoundingMode.HALF_EVEN);
            }

            if (value.compareTo(BigDecimal.ZERO) > 0) {
                left = mid;
            } else {
                right = mid;
            }

            if (right.subtract(left).abs().compareTo(PRECISION) < 0) {
                return mid.setScale(8, RoundingMode.HALF_EVEN);
            }
        }

        throw new IllegalArgumentException("XIRR calculation did not converge using bisection method");
    }

    private static BigDecimal computeXirrFunction(List<BigDecimal> cashFlows, List<LocalDate> dates, BigDecimal rate) {
        BigDecimal result = BigDecimal.ZERO;
        LocalDate initialDate = dates.getFirst();

        for (int i = 0; i < cashFlows.size(); i++) {
            BigDecimal timeFactor = calculateTimeFactor(initialDate, dates.get(i));
            BigDecimal denominator = calculatePowerTerm(rate, timeFactor);

            // cashFlow / denominator
            BigDecimal term = cashFlows.get(i).divide(denominator, MC);
            result = result.add(term);
        }

        return result;
    }

    private static BigDecimal computeXirrDerivative(
            List<BigDecimal> cashFlows, List<LocalDate> dates, BigDecimal rate) {
        BigDecimal result = BigDecimal.ZERO;
        LocalDate initialDate = dates.getFirst();

        for (int i = 0; i < cashFlows.size(); i++) {
            BigDecimal timeFactor = calculateTimeFactor(initialDate, dates.get(i));

            // exponent * cashFlow
            BigDecimal numerator = timeFactor.multiply(cashFlows.get(i).negate());

            BigDecimal exponentPlusOne = timeFactor.add(BigDecimal.ONE);
            BigDecimal denominator = calculatePowerTerm(rate, exponentPlusOne);

            // numerator / denominator
            BigDecimal term = numerator.divide(denominator, MC);
            result = result.add(term);
        }

        return result;
    }

    /**
     * Calculates the time factor between two dates (days between / days in year)
     *
     * @param initialDate The starting date
     * @param currentDate The date to calculate factor for
     * @return The time factor as a BigDecimal
     */
    private static BigDecimal calculateTimeFactor(LocalDate initialDate, LocalDate currentDate) {
        BigDecimal daysFromStart = new BigDecimal(ChronoUnit.DAYS.between(initialDate, currentDate));
        return daysFromStart.divide(DAYS_IN_YEAR, MC);
    }

    /**
     * Calculates (1 + rate)^exponent with support for fractional exponents
     *
     * @param rate The rate to use in calculation
     * @param exponent The exponent to raise (1 + rate) to
     * @return The calculated power term
     */
    private static BigDecimal calculatePowerTerm(BigDecimal rate, BigDecimal exponent) {
        BigDecimal base = BigDecimal.ONE.add(rate);

        // Handle the integer part of the exponent
        BigDecimal result = base.pow(exponent.intValue(), MC);

        // Handle fractional exponents if present
        if (exponent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal fractionalPart = exponent.subtract(new BigDecimal(exponent.intValue()));
            BigDecimal extraFactor = bigDecimalPow(base, fractionalPart);
            result = result.multiply(extraFactor, MC);
        }

        return result;
    }

    /**
     * Calculate base^exponent for decimal exponents using the property:
     * a^b = e^(b*ln(a))
     *
     * @param base The base number
     * @param exponent The exponent (can be fractional)
     * @return The result of base raised to the power of exponent
     */
    private static BigDecimal bigDecimalPow(BigDecimal base, BigDecimal exponent) {
        if (exponent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }

        if (exponent.compareTo(BigDecimal.ONE) == 0) {
            return base;
        }

        // For integer exponents, use built-in pow method
        if (exponent.stripTrailingZeros().scale() <= 0) {
            return base.pow(exponent.intValue(), MC);
        }

        // For fractional exponents, use approximation
        double baseDouble = base.doubleValue();
        double exponentDouble = exponent.doubleValue();
        double resultDouble = Math.pow(baseDouble, exponentDouble);

        return new BigDecimal(resultDouble, MC);
    }
}
