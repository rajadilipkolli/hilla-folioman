package com.app.folioman.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mockStatic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LocalDateUtilityTest {

    @Test
    void testParseWithDefaultFormat() {
        // Given a date string in DD-MMM-YYYY format
        String dateString = "26-May-2025";

        // When parsing the date string
        LocalDate result = LocalDateUtility.parse(dateString);

        // Then the result should be correct
        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 26));
    }

    @Test
    void testParseWithCustomFormat() {
        // Given a date string in YYYY-MM-DD format
        String dateString = "2025-05-26";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // When parsing the date string with a custom formatter
        LocalDate result = LocalDateUtility.parse(dateString, formatter);

        // Then the result should be correct
        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 26));
    }

    @Test
    void testParseWithExtraSpaces() {
        // Given a date string with extra spaces
        String dateString = "26-May-2025  "; // Only adding spaces at the end is supported

        // When parsing the date string
        LocalDate result = LocalDateUtility.parse(dateString);

        // Then the result should be correct with spaces trimmed
        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 26));
    }

    @Test
    void testParseInvalidDate() {
        // Given an invalid date string
        String dateString = "32-May-2025";

        // When parsing the date string, then an exception should be thrown
        assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() -> LocalDateUtility.parse(dateString));
    }

    @Test
    void testGetAdjustedDateBeforeElevenThirty() {
        // Given a fixed current date-time before 11:30 PM
        LocalDateTime mockDateTime = LocalDateTime.of(2025, 5, 26, 10, 0); // Monday, May 26, 2025, 10:00 AM
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockDateTime);

            // When getting the adjusted date
            LocalDate result = LocalDateUtility.getAdjustedDate();

            // Then the date should be the previous day (Sunday)
            // And since Sunday is a weekend, it should be adjusted to Friday
            assertThat(result).isEqualTo(LocalDate.of(2025, 5, 23)); // Friday
        }
    }

    @Test
    void testGetAdjustedDateAfterElevenThirty() {
        // Given a fixed current date-time after 11:30 PM
        LocalDateTime mockDateTime = LocalDateTime.of(2025, 5, 26, 23, 45); // Monday, May 26, 2025, 11:45 PM
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockDateTime);

            // When getting the adjusted date
            LocalDate result = LocalDateUtility.getAdjustedDate();

            // Then the date should be the current day (Monday)
            assertThat(result).isEqualTo(LocalDate.of(2025, 5, 26)); // Monday
        }
    }

    @Test
    void testGetAdjustedDateForWeekend() {
        // Given a weekend date (Saturday)
        LocalDate saturdayDate = LocalDate.of(2025, 5, 24); // Saturday

        // When getting the adjusted date
        LocalDate result = LocalDateUtility.getAdjustedDate(saturdayDate);

        // Then it should return the previous Friday
        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 23)); // Friday
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @Test
    void testGetAdjustedDateForWeekday() {
        // Given a weekday date (Wednesday)
        LocalDate wednesdayDate = LocalDate.of(2025, 5, 21); // Wednesday

        // When getting the adjusted date
        LocalDate result = LocalDateUtility.getAdjustedDate(wednesdayDate);

        // Then it should return the same date
        assertThat(result).isEqualTo(wednesdayDate);
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    void testGetAdjustedDateOrDefaultWithNullInput() {
        // Given a fixed current date
        LocalDateTime mockDateTime = LocalDateTime.of(2025, 5, 26, 10, 0); // Monday
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockDateTime);

            // When getting the adjusted date with null input
            LocalDate result = LocalDateUtility.getAdjustedDateOrDefault(null);

            // Then it should return the adjusted date based on the current date
            assertThat(result).isEqualTo(LocalDate.of(2025, 5, 23)); // Friday before Monday
        }
    }

    @Test
    void testGetAdjustedDateOrDefaultWithNonNullInput() {
        // Given a fixed date
        LocalDate inputDate = LocalDate.of(2025, 5, 24); // Saturday

        // When getting the adjusted date with non-null input
        LocalDate result = LocalDateUtility.getAdjustedDateOrDefault(inputDate);

        // Then it should return the adjusted date based on the input date
        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 23)); // Friday before Saturday
    }

    @Test
    void testGetYesterday() {
        // We need a different approach since we're having issues with the static mock
        // Let's just verify that getYesterday returns a date that is 1 day before today

        // Get today's date
        LocalDate today = LocalDate.now();

        // Get yesterday from the utility method
        LocalDate yesterday = LocalDateUtility.getYesterday();

        // Verify that yesterday is exactly 1 day before today
        assertThat(yesterday).isEqualTo(today.minusDays(1));
    }
}
