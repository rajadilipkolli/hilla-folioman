package com.app.folioman.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CommonConstantsTest {

    @Test
    void testFormatterDdMmmYyyyNotNull() {
        assertNotNull(CommonConstants.FORMATTER_DD_MMM_YYYY);
    }

    @Test
    void testFormatterMmmDYyyyNotNull() {
        assertNotNull(CommonConstants.FORMATTER_MMM_D_YYYY);
    }

    @Test
    void testFormatterDdMmmYyyyFormat() {
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        String formatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        assertEquals("15-Mar-2024", formatted);
    }

    @Test
    void testFormatterMmmDYyyyFormat() {
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        String formatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        assertEquals("Mar 15 2024", formatted);
    }

    @Test
    void testFormatterDdMmmYyyyParse() {
        String dateString = "25-Dec-2023";
        LocalDate parsed = LocalDate.parse(dateString, CommonConstants.FORMATTER_DD_MMM_YYYY);
        assertEquals(LocalDate.of(2023, 12, 25), parsed);
    }

    @Test
    void testFormatterMmmDYyyyParse() {
        String dateString = "Jan 1 2025";
        LocalDate parsed = LocalDate.parse(dateString, CommonConstants.FORMATTER_MMM_D_YYYY);
        assertEquals(LocalDate.of(2025, 1, 1), parsed);
    }

    @Test
    void testFormatterDdMmmYyyyLocale() {
        DateTimeFormatter expectedFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        String actualFormatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        String expectedFormatted = expectedFormatter.format(testDate);

        assertEquals(expectedFormatted, actualFormatted);
    }

    @Test
    void testFormatterMmmDYyyyLocale() {
        DateTimeFormatter expectedFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        String actualFormatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        String expectedFormatted = expectedFormatter.format(testDate);

        assertEquals(expectedFormatted, actualFormatted);
    }

    @Test
    void testFormatterDdMmmYyyyWithSingleDigitDay() {
        LocalDate testDate = LocalDate.of(2024, 7, 5);
        String formatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        assertEquals("05-Jul-2024", formatted);
    }

    @Test
    void testFormatterMmmDYyyyWithSingleDigitDay() {
        LocalDate testDate = LocalDate.of(2024, 7, 5);
        String formatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        assertEquals("Jul 5 2024", formatted);
    }
}
