package com.app.folioman.mfschemes;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class NavNotFoundExceptionTest {

    @Test
    void testConstructorWithValidMessageAndDate() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertEquals("NAV not found on 2023-12-25", exception.getMessage());
        assertEquals(date, exception.getNavDate());
    }

    @Test
    void testConstructorWithNullMessage() {
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(null, date);

        assertEquals("null on 2023-12-25", exception.getMessage());
        assertEquals(date, exception.getNavDate());
    }

    @Test
    void testConstructorWithNullDate() {
        String message = "NAV not found";

        assertThrows(NullPointerException.class, () -> {
            new NavNotFoundException(message, null);
        });
    }

    @Test
    void testConstructorWithEmptyMessage() {
        String message = "";
        LocalDate date = LocalDate.of(2023, 12, 25);

        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertEquals(" on 2023-12-25", exception.getMessage());
        assertEquals(date, exception.getNavDate());
    }

    @Test
    void testGetDate() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 1, 1);
        NavNotFoundException exception = new NavNotFoundException(message, date);

        LocalDate result = exception.getNavDate();

        assertEquals(date, result);
        assertSame(date, result);
    }

    @Test
    void testMessageFormattingWithDifferentDates() {
        String message = "Test message";
        LocalDate date1 = LocalDate.of(2020, 2, 29); // leap year
        LocalDate date2 = LocalDate.of(2023, 12, 31); // end of year

        NavNotFoundException exception1 = new NavNotFoundException(message, date1);
        NavNotFoundException exception2 = new NavNotFoundException(message, date2);

        assertEquals("Test message on 2020-02-29", exception1.getMessage());
        assertEquals("Test message on 2023-12-31", exception2.getMessage());
    }

    @Test
    void testInheritanceFromRuntimeException() {
        String message = "NAV not found";
        LocalDate date = LocalDate.of(2023, 12, 25);
        NavNotFoundException exception = new NavNotFoundException(message, date);

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}
