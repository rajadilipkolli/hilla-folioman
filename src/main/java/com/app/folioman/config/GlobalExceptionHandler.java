package com.app.folioman.config;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .map(objectError -> {
                    FieldError fieldError = (FieldError) objectError;
                    return new ApiValidationError(
                            fieldError.getObjectName(),
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            Objects.requireNonNullElse(fieldError.getDefaultMessage(), ""));
                })
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    @ExceptionHandler(SchemeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail onException(SchemeNotFoundException schemeNotFoundException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), schemeNotFoundException.getMessage());
        problemDetail.setTitle("Scheme NotFound");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/scheme-not-found"));
        return problemDetail;
    }

    @ExceptionHandler(NavNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail onException(NavNotFoundException navNotFoundException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), navNotFoundException.getMessage());
        problemDetail.setTitle("NAV Not Found");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/nav-not-found"));
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(ConstraintViolationException constraintViolationException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(400), constraintViolationException.getMessage());
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/validation-error"));
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
