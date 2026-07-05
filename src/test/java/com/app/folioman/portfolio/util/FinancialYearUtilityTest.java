package com.app.folioman.portfolio.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FinancialYearUtilityTest {

    @Test
    void shouldReturnCurrentYearWhenDateIsAprilFirstOrLater() {
        LocalDate date = LocalDate.of(2023, 4, 1);
        FinancialYearUtility.FinancialYear fy = FinancialYearUtility.getFinancialYearForDate(date);

        assertThat(fy.startDate()).isEqualTo(LocalDate.of(2023, 4, 1));
        assertThat(fy.endDate()).isEqualTo(LocalDate.of(2024, 3, 31));
    }

    @Test
    void shouldReturnPreviousYearWhenDateIsBeforeAprilFirst() {
        LocalDate date = LocalDate.of(2023, 3, 31);
        FinancialYearUtility.FinancialYear fy = FinancialYearUtility.getFinancialYearForDate(date);

        assertThat(fy.startDate()).isEqualTo(LocalDate.of(2022, 4, 1));
        assertThat(fy.endDate()).isEqualTo(LocalDate.of(2023, 3, 31));
    }

    @Test
    void shouldReturnCorrectFinancialYearByStartYear() {
        FinancialYearUtility.FinancialYear fy = FinancialYearUtility.getFinancialYearByStartYear(2023);

        assertThat(fy.startDate()).isEqualTo(LocalDate.of(2023, 4, 1));
        assertThat(fy.endDate()).isEqualTo(LocalDate.of(2024, 3, 31));
    }
}
