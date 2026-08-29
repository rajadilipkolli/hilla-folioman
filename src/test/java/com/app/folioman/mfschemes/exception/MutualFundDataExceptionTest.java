package com.app.folioman.mfschemes.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MutualFundDataExceptionTest {

    @Test
    void constructorWithMessage() {
        String message = "Test error message";
        MutualFundDataException exception = new MutualFundDataException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithNullMessage() {
        MutualFundDataException exception = new MutualFundDataException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithEmptyMessage() {
        String message = "";
        MutualFundDataException exception = new MutualFundDataException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new IllegalArgumentException("Root cause");
        MutualFundDataException exception = new MutualFundDataException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void constructorWithMessageAndNullCause() {
        String message = "Test error message";
        MutualFundDataException exception = new MutualFundDataException(message, null);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithNullMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Root cause");
        MutualFundDataException exception = new MutualFundDataException(null, cause);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void inheritanceFromRuntimeException() {
        MutualFundDataException exception = new MutualFundDataException("Test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }
}
