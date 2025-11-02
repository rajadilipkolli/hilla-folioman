package com.app.folioman.mfschemes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SchemeNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Scheme not found";
        SchemeNotFoundException exception = new SchemeNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithNullMessage() {
        SchemeNotFoundException exception = new SchemeNotFoundException(null);

        assertNull(exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithEmptyMessage() {
        String message = "";
        SchemeNotFoundException exception = new SchemeNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testInheritanceFromRuntimeException() {
        SchemeNotFoundException exception = new SchemeNotFoundException("test");

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}
