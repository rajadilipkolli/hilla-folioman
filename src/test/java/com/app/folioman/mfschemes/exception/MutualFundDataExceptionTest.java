package com.app.folioman.mfschemes.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MutualFundDataExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";
        MutualFundDataException exception = new MutualFundDataException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithNullMessage() {
        MutualFundDataException exception = new MutualFundDataException(null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        String message = "";
        MutualFundDataException exception = new MutualFundDataException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new IllegalArgumentException("Root cause");
        MutualFundDataException exception = new MutualFundDataException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndNullCause() {
        String message = "Test error message";
        MutualFundDataException exception = new MutualFundDataException(message, null);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithNullMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Root cause");
        MutualFundDataException exception = new MutualFundDataException(null, cause);

        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInheritanceFromRuntimeException() {
        MutualFundDataException exception = new MutualFundDataException("Test");

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}
