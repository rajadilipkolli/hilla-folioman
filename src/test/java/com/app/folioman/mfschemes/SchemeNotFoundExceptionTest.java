package com.app.folioman.mfschemes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SchemeNotFoundExceptionTest {

    @Test
    void constructorWithMessage() {
        String message = "Scheme not found";
        SchemeNotFoundException exception = new SchemeNotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructorWithNullMessage() {
        SchemeNotFoundException exception = new SchemeNotFoundException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructorWithEmptyMessage() {
        String message = "";
        SchemeNotFoundException exception = new SchemeNotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void inheritanceFromRuntimeException() {
        SchemeNotFoundException exception = new SchemeNotFoundException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }
}
