package com.app.folioman.config;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiValidationError(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        Objects.requireNonNullElse(fieldError.getDefaultMessage(), "")))
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(SchemeNotFoundException.class)
    ProblemDetail onException(SchemeNotFoundException schemeNotFoundException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, schemeNotFoundException.getMessage());
        problemDetail.setTitle("Scheme NotFound");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/scheme-not-found"));
        return problemDetail;
    }

    @ExceptionHandler(NavNotFoundException.class)
    ProblemDetail onException(NavNotFoundException navNotFoundException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, navNotFoundException.getMessage());
        problemDetail.setTitle("NAV Not Found");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/nav-not-found"));
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail onException(ConstraintViolationException constraintViolationException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, constraintViolationException.getMessage());
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.hilla-folioman.com/errors/validation-error"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e) {
        LOGGER.error("Unexpected error occurred", e);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
