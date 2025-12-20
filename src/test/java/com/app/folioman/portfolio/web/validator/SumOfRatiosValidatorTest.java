package com.app.folioman.portfolio.web.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.app.folioman.portfolio.models.request.Fund;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SumOfRatiosValidatorTest {

    private SumOfRatiosValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new SumOfRatiosValidator();
        lenient()
                .when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(builder);
    }

    @Test
    void shouldReturnTrueWhenListIsNull() {
        // When validating a null list
        boolean result = validator.isValid(null, context);

        // Then the validation should pass
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenListIsEmpty() {
        // When validating an empty list
        boolean result = validator.isValid(Collections.emptyList(), context);

        // Then the validation should pass
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenSumOfRatiosEqualsOne() {
        // Given a list of funds with ratios summing to 1
        List<Fund> funds = Arrays.asList(new Fund(100.0, 0.3), new Fund(200.0, 0.4), new Fund(300.0, 0.3));

        // When validating the list
        boolean result = validator.isValid(funds, context);

        // Then the validation should pass
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWithSmallFloatingPointDifference() {
        // Given a list of funds with ratios summing to slightly less than 1 due to floating point precision
        List<Fund> funds = Arrays.asList(new Fund(100.0, 0.33), new Fund(200.0, 0.33), new Fund(300.0, 0.34));
        // Sum is actually 0.9999999999999999 due to floating point precision

        // When validating the list
        boolean result = validator.isValid(funds, context);

        // Then the validation should pass due to tolerance for floating point errors
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSumOfRatiosLessThanOne() {
        // Given a list of funds with ratios summing to less than 1
        List<Fund> funds = Arrays.asList(new Fund(100.0, 0.2), new Fund(200.0, 0.3), new Fund(300.0, 0.4));
        // Sum is 0.9

        // When validating the list
        boolean result = validator.isValid(funds, context);

        // Then the validation should fail
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.contains("0.90"));
        verify(builder).addConstraintViolation();
    }

    @Test
    void shouldReturnFalseWhenSumOfRatiosGreaterThanOne() {
        // Given a list of funds with ratios summing to greater than 1
        List<Fund> funds = Arrays.asList(new Fund(100.0, 0.4), new Fund(200.0, 0.4), new Fund(300.0, 0.4));
        // Sum is 1.2

        // When validating the list
        boolean result = validator.isValid(funds, context);

        // Then the validation should fail
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.contains("1.20"));
        verify(builder).addConstraintViolation();
    }

    @Test
    void shouldHandleVeryLargeListOfFunds() {
        // Given a very large list of funds
        List<Fund> funds = new ArrayList<>();
        double smallRatio = 1.0 / 1000.0;
        for (int i = 0; i < 1000; i++) {
            funds.add(new Fund(100.0 * i, smallRatio));
        }

        // When validating the list
        boolean result = validator.isValid(funds, context);

        // Then the validation should pass
        assertThat(result).isTrue();
    }
}
