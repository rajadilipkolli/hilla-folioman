package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MfApiPropertiesTest {

    private static Validator validator;
    private static ValidatorFactory factory;

    @BeforeAll
    public static void setupValidatorInstance() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    public static void closeValidatorFactory() {
        if (factory != null) {
            factory.close();
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://example.com/mfapi",
                "http://localhost:8080/mfapi",
                "https://api.example.com/mfapi?version=v1",
                "https://example.com:443/mfapi"
            })
    void whenDataUrlIsValid_thenNoValidationErrors(String url) {
        MfApiProperties mfApi = new MfApiProperties();
        mfApi.setDataUrl(url);

        Set<ConstraintViolation<MfApiProperties>> violations = validator.validate(mfApi);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenDataUrlIsInvalid_thenValidationFails() {
        MfApiProperties mfApi = new MfApiProperties();
        mfApi.setDataUrl("");

        Set<ConstraintViolation<MfApiProperties>> violations = validator.validate(mfApi);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-url", "ftp://example.com", "not_a_url"})
    void whenDataUrlIsInvalid_thenValidationFails(String invalidUrl) {
        MfApiProperties mfApi = new MfApiProperties();
        mfApi.setDataUrl(invalidUrl);

        Set<ConstraintViolation<MfApiProperties>> violations = validator.validate(mfApi);
        assertFalse(violations.isEmpty());
        ConstraintViolation<MfApiProperties> violation = violations.iterator().next();
        assertEquals("Data URL must be a valid HTTP(S) URL", violation.getMessage());
        assertEquals("dataUrl", violation.getPropertyPath().toString());
    }
}
