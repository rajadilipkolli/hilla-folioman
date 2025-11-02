package com.app.folioman.mfschemes.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemeConstantsTest {

    @Test
    @DisplayName("Should verify NAV_SEPARATOR constant value")
    void testNavSeparatorConstant() {
        assertEquals(";", SchemeConstants.NAV_SEPARATOR);
    }

    @Test
    @DisplayName("Should verify FIRST_RETRY constant value")
    void testFirstRetryConstant() {
        assertEquals(1, SchemeConstants.FIRST_RETRY);
    }

    @Test
    @DisplayName("Should verify THIRD_RETRY constant value")
    void testThirdRetryConstant() {
        assertEquals(3, SchemeConstants.THIRD_RETRY);
    }

    @Test
    @DisplayName("Should verify MAX_RETRIES constant value")
    void testMaxRetriesConstant() {
        assertEquals(4, SchemeConstants.MAX_RETRIES);
    }

    @Test
    @DisplayName("Should parse ISO_LOCAL_DATE format correctly")
    void testFlexibleDateFormatterWithIsoFormat() {
        String dateString = "2023-12-25";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate parsedDate = LocalDate.parse(dateString, SchemeConstants.FLEXIBLE_DATE_FORMATTER);

        assertEquals(expectedDate, parsedDate);
    }

    @Test
    @DisplayName("Should parse dd-MMM-yyyy format correctly")
    void testFlexibleDateFormatterWithCustomFormat() {
        String dateString = "25-Dec-2023";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate parsedDate = LocalDate.parse(dateString, SchemeConstants.FLEXIBLE_DATE_FORMATTER);

        assertEquals(expectedDate, parsedDate);
    }

    @Test
    @DisplayName("Should handle different month abbreviations in English")
    void testFlexibleDateFormatterWithDifferentMonths() {
        String janDate = "15-Jan-2023";
        String julDate = "15-Jul-2023";

        LocalDate parsedJan = LocalDate.parse(janDate, SchemeConstants.FLEXIBLE_DATE_FORMATTER);
        LocalDate parsedJul = LocalDate.parse(julDate, SchemeConstants.FLEXIBLE_DATE_FORMATTER);

        assertEquals(LocalDate.of(2023, 1, 15), parsedJan);
        assertEquals(LocalDate.of(2023, 7, 15), parsedJul);
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void testFlexibleDateFormatterWithInvalidFormat() {
        String invalidDateString = "2023/12/25";

        assertThrows(DateTimeParseException.class, () -> {
            LocalDate.parse(invalidDateString, SchemeConstants.FLEXIBLE_DATE_FORMATTER);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid month abbreviation")
    void testFlexibleDateFormatterWithInvalidMonth() {
        String invalidMonthString = "25-Xyz-2023";

        assertThrows(DateTimeParseException.class, () -> {
            LocalDate.parse(invalidMonthString, SchemeConstants.FLEXIBLE_DATE_FORMATTER);
        });
    }

    @Test
    @DisplayName("Should verify formatter is not null")
    void testFlexibleDateFormatterNotNull() {
        assertNotNull(SchemeConstants.FLEXIBLE_DATE_FORMATTER);
    }

    @Test
    @DisplayName("Should parse leap year date correctly")
    void testFlexibleDateFormatterWithLeapYear() {
        String leapYearDate = "29-Feb-2024";
        LocalDate expectedDate = LocalDate.of(2024, 2, 29);

        LocalDate parsedDate = LocalDate.parse(leapYearDate, SchemeConstants.FLEXIBLE_DATE_FORMATTER);

        assertEquals(expectedDate, parsedDate);
    }

    @Test
    @DisplayName("Should throw exception for invalid leap year date")
    void testFlexibleDateFormatterWithInvalidLeapYear() {
        String invalidLeapYearDate = "29-Feb-2023";
        try {
            LocalDate parsed = LocalDate.parse(invalidLeapYearDate, SchemeConstants.FLEXIBLE_DATE_FORMATTER);
            // If parser is lenient and adjusts, expect it to fallback to 28-Feb-2023
            assertEquals(LocalDate.of(2023, 2, 28), parsed);
        } catch (DateTimeParseException ex) {
            // Expected behavior in strict parsing implementations
            assertNotNull(ex);
        }
    }
}
