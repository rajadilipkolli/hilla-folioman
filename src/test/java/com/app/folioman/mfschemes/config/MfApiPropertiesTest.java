package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MfApiPropertiesTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenDataUrlIsValid_thenNoValidationErrors() {
        MfApiProperties mfApi = new MfApiProperties();
        mfApi.setDataUrl("https://example.com/mfapi");

        Set<ConstraintViolation<MfApiProperties>> violations = validator.validate(mfApi);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenDataUrlIsInvalid_thenValidationFails() {
        MfApiProperties mfApi = new MfApiProperties();
        mfApi.setDataUrl("invalid-url");

        Set<ConstraintViolation<MfApiProperties>> violations = validator.validate(mfApi);
        assertFalse(violations.isEmpty());
    }
}
