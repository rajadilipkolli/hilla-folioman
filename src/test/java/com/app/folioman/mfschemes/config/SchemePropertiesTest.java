package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SchemePropertiesTest {
    private static Validator validator;
    private static ValidatorFactory factory;

    @BeforeAll
    static void setupValidatorInstance() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cleanupValidatorFactory() {
        factory.close();
    }

    @Test
    void whenDataUrlIsValid_thenNoValidationErrors() {
        SchemeProperties scheme = new SchemeProperties();
        scheme.setDataUrl("https://example.com/scheme");

        Set<ConstraintViolation<SchemeProperties>> violations = validator.validate(scheme);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenDataUrlIsBlank_thenValidationFails() {
        SchemeProperties scheme = new SchemeProperties();
        scheme.setDataUrl("");

        Set<ConstraintViolation<SchemeProperties>> violations = validator.validate(scheme);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenDataUrlIsInvalid_thenValidationFails() {
        SchemeProperties scheme = new SchemeProperties();
        scheme.setDataUrl("invalid-url");

        Set<ConstraintViolation<SchemeProperties>> violations = validator.validate(scheme);
        assertThat(violations).isNotEmpty();
    }
}
