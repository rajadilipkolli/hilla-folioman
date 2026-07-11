package com.app.folioman.portfolio.util;

import java.time.LocalDate;
import java.time.Month;
import org.jspecify.annotations.NonNull;

/**
 * Utility class for Financial Year calculations.
 */
public final class FinancialYearUtility {

    private FinancialYearUtility() {
        // Private constructor for utility class
    }

    /**
     * Represents a Financial Year with a start and end date.
     */
    public record FinancialYear(LocalDate startDate, LocalDate endDate) {}

    /**
     * Gets the financial year for a given date.
     * In India, the financial year starts on April 1 and ends on March 31 of the following year.
     *
     * @param date the date for which to determine the financial year
     * @return the FinancialYear containing the start and end dates
     */
    public static FinancialYear getFinancialYearForDate(@NonNull LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        int year = date.getYear();
        if (date.getMonthValue() < Month.APRIL.getValue()) {
            return new FinancialYear(LocalDate.of(year - 1, Month.APRIL, 1), LocalDate.of(year, Month.MARCH, 31));
        } else {
            return new FinancialYear(LocalDate.of(year, Month.APRIL, 1), LocalDate.of(year + 1, Month.MARCH, 31));
        }
    }

    /**
     * Gets the financial year by its starting year.
     * For example, for FY 2023-24, the starting year is 2023.
     *
     * @param startYear the calendar year in which the financial year starts
     * @return the FinancialYear containing the start and end dates
     */
    public static FinancialYear getFinancialYearByStartYear(int startYear) {
        return new FinancialYear(LocalDate.of(startYear, Month.APRIL, 1), LocalDate.of(startYear + 1, Month.MARCH, 31));
    }
}
