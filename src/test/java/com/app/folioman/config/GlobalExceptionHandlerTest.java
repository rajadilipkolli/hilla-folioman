package com.app.folioman.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void onException_MethodArgumentNotValidException_ShouldReturnProblemDetail() {

        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getObjectName()).thenReturn("testObject");
        when(fieldError.getField()).thenReturn("testField");
        when(fieldError.getRejectedValue()).thenReturn("rejectedValue");
        when(fieldError.getDefaultMessage()).thenReturn("Test error message");
        when(methodArgumentNotValidException.getAllErrors()).thenReturn(List.of(fieldError));

        ProblemDetail result = globalExceptionHandler.onException(methodArgumentNotValidException);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("Invalid request content.");
        assertThat(result.getTitle()).isEqualTo("Constraint Violation");
        assertThat(result.getProperties().get("violations")).isNotNull();

        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ApiValidationError> violations = (List<GlobalExceptionHandler.ApiValidationError>)
                result.getProperties().get("violations");
        assertThat(violations).hasSize(1);
        assertThat(violations.getFirst().object()).isEqualTo("testObject");
        assertThat(violations.getFirst().field()).isEqualTo("testField");
        assertThat(violations.getFirst().rejectedValue()).isEqualTo("rejectedValue");
        assertThat(violations.getFirst().message()).isEqualTo("Test error message");
    }

    @Test
    void onException_MethodArgumentNotValidException_WithNullMessage_ShouldUseEmptyString() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getObjectName()).thenReturn("testObject");
        when(fieldError.getField()).thenReturn("testField");
        when(fieldError.getRejectedValue()).thenReturn("rejectedValue");
        when(fieldError.getDefaultMessage()).thenReturn(null);
        when(methodArgumentNotValidException.getAllErrors()).thenReturn(List.of(fieldError));

        ProblemDetail result = globalExceptionHandler.onException(methodArgumentNotValidException);

        assertThat(result).isNotNull();
        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ApiValidationError> violations = (List<GlobalExceptionHandler.ApiValidationError>)
                result.getProperties().get("violations");
        assertThat(violations.getFirst().message()).isEmpty();
    }

    @Test
    void onException_MethodArgumentNotValidException_WithMultipleErrors_ShouldSortByField() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        FieldError fieldError1 = mock(FieldError.class);
        FieldError fieldError2 = mock(FieldError.class);

        when(fieldError1.getObjectName()).thenReturn("testObject");
        when(fieldError1.getField()).thenReturn("zField");
        when(fieldError1.getRejectedValue()).thenReturn("value1");
        when(fieldError1.getDefaultMessage()).thenReturn("message1");

        when(fieldError2.getObjectName()).thenReturn("testObject");
        when(fieldError2.getField()).thenReturn("aField");
        when(fieldError2.getRejectedValue()).thenReturn("value2");
        when(fieldError2.getDefaultMessage()).thenReturn("message2");

        when(methodArgumentNotValidException.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ProblemDetail result = globalExceptionHandler.onException(methodArgumentNotValidException);

        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ApiValidationError> violations = (List<GlobalExceptionHandler.ApiValidationError>)
                result.getProperties().get("violations");
        assertThat(violations).hasSize(2);
        assertThat(violations.get(0).field()).isEqualTo("aField");
        assertThat(violations.get(1).field()).isEqualTo("zField");
    }

    @Test
    void onException_SchemeNotFoundException_ShouldReturnProblemDetail() {
        String errorMessage = "Scheme not found";
        SchemeNotFoundException exception = new SchemeNotFoundException(errorMessage);

        ProblemDetail result = globalExceptionHandler.onException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getTitle()).isEqualTo("Scheme NotFound");
    }

    @Test
    void onException_NavNotFoundException_ShouldReturnProblemDetail() {
        String errorMessage = "NAV not found";
        NavNotFoundException exception = new NavNotFoundException(errorMessage, LocalDate.of(2025, 11, 2));

        ProblemDetail result = globalExceptionHandler.onException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).isEqualTo(errorMessage + " on 2025-11-02");
        assertThat(result.getTitle()).isEqualTo("NAV Not Found");
    }

    @Test
    void onException_ConstraintViolationException_ShouldReturnProblemDetail() {
        String errorMessage = "Constraint violation occurred";
        ConstraintViolationException exception = new ConstraintViolationException(errorMessage, null);

        ProblemDetail result = globalExceptionHandler.onException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getTitle()).isEqualTo("Constraint Violation");
    }
}
