package com.app.folioman.mfschemes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class NavNotFoundExceptionTest {

    @Test
    void constructorWithValidMessageAndDate() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertThat(exception.getMessage()).isEqualTo("NAV not found on 2023-12-25");
        assertThat(exception.getNavDate()).isEqualTo(date);
    }

    @Test
    void constructorWithNullMessage() {
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(null, date);

        assertThat(exception.getMessage()).isEqualTo("null on 2023-12-25");
        assertThat(exception.getNavDate()).isEqualTo(date);
    }

    @Test
    void constructorWithNullDate() {
        String message = "NAV not found";

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            new NavNotFoundException(message, null);
        });
    }

    @Test
    void constructorWithEmptyMessage() {
        String message = "";
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertThat(exception.getMessage()).isEqualTo(" on 2023-12-25");
        assertThat(exception.getNavDate()).isEqualTo(date);
    }

    @Test
    void getDate() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 1, 1);
        NavNotFoundException exception = new NavNotFoundException(message, date);

        LocalDate result = exception.getNavDate();

        assertThat(result).isEqualTo(date).isSameAs(date);
    }

    @Test
    void messageFormattingWithDifferentDates() {
        String message = "Test message";
        LocalDate date1 = LocalDate.of(2020, 2, 29); // leap year
        LocalDate date2 = LocalDate.of(2023, 12, 31); // end of year

        NavNotFoundException exception1 = new NavNotFoundException(message, date1);
        NavNotFoundException exception2 = new NavNotFoundException(message, date2);

        assertThat(exception1.getMessage()).isEqualTo("Test message on 2020-02-29");
        assertThat(exception2.getMessage()).isEqualTo("Test message on 2023-12-31");
    }

    @Test
    void inheritanceFromRuntimeException() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 12, 25);
        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }
}
