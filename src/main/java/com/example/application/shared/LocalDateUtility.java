package com.example.application.shared;

import static com.example.application.shared.CommonConstants.FORMATTER_DD_MMM_YYYY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class LocalDateUtility {

    public static LocalDate parse(String from) {
        return LocalDate.parse(from, FORMATTER_DD_MMM_YYYY);
    }

    public static LocalDate getAdjustedDate() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // NAVs are refreshed only after 11:30 PM so reduce the day by 1
        if (currentDateTime.toLocalTime().isBefore(LocalTime.of(23, 30))) {
            currentDateTime = currentDateTime.minusDays(1);
        }
        return getAdjustedDate(currentDateTime.toLocalDate());
    }

    public static LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }

    public static LocalDate getAdjustedDateOrDefault(LocalDate asOfDate) {
        return asOfDate == null ? getAdjustedDate() : getAdjustedDate(asOfDate);
    }
}
