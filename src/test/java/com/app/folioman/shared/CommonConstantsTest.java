package com.app.folioman.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CommonConstantsTest {

    @Test
    void formatterDdMmmYyyyNotNull() {
        assertThat(CommonConstants.FORMATTER_DD_MMM_YYYY).isNotNull();
    }

    @Test
    void formatterMmmDYyyyNotNull() {
        assertThat(CommonConstants.FORMATTER_MMM_D_YYYY).isNotNull();
    }

    @Test
    void formatterDdMmmYyyyFormat() {
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        String formatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        assertThat(formatted).isEqualTo("15-Mar-2024");
    }

    @Test
    void formatterMmmDYyyyFormat() {
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        String formatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        assertThat(formatted).isEqualTo("Mar 15 2024");
    }

    @Test
    void formatterDdMmmYyyyParse() {
        String dateString = "25-Dec-2023";
        LocalDate parsed = LocalDate.parse(dateString, CommonConstants.FORMATTER_DD_MMM_YYYY);
        assertThat(parsed).isEqualTo(LocalDate.of(2023, 12, 25));
    }

    @Test
    void formatterMmmDYyyyParse() {
        String dateString = "Jan 1 2025";
        LocalDate parsed = LocalDate.parse(dateString, CommonConstants.FORMATTER_MMM_D_YYYY);
        assertThat(parsed).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void formatterDdMmmYyyyLocale() {
        DateTimeFormatter expectedFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        String actualFormatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        String expectedFormatted = expectedFormatter.format(testDate);

        assertThat(actualFormatted).isEqualTo(expectedFormatted);
    }

    @Test
    void formatterMmmDYyyyLocale() {
        DateTimeFormatter expectedFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        String actualFormatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        String expectedFormatted = expectedFormatter.format(testDate);

        assertThat(actualFormatted).isEqualTo(expectedFormatted);
    }

    @Test
    void formatterDdMmmYyyyWithSingleDigitDay() {
        LocalDate testDate = LocalDate.of(2024, 7, 5);
        String formatted = CommonConstants.FORMATTER_DD_MMM_YYYY.format(testDate);
        assertThat(formatted).isEqualTo("05-Jul-2024");
    }

    @Test
    void formatterMmmDYyyyWithSingleDigitDay() {
        LocalDate testDate = LocalDate.of(2024, 7, 5);
        String formatted = CommonConstants.FORMATTER_MMM_D_YYYY.format(testDate);
        assertThat(formatted).isEqualTo("Jul 5 2024");
    }
}
