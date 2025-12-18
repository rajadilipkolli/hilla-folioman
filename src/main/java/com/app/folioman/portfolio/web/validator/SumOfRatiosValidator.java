package com.app.folioman.portfolio.web.validator;

import com.app.folioman.portfolio.models.request.Fund;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class SumOfRatiosValidator implements ConstraintValidator<ValidSumOfRatios, List<Fund>> {

    @Override
    public boolean isValid(List<Fund> funds, ConstraintValidatorContext context) {
        if (funds == null || funds.isEmpty()) {
            return true;
        }

        double totalRatio = funds.stream().mapToDouble(Fund::ratio).sum();

        // Check if the sum of ratios is approximately 1
        boolean isValid = Math.abs(totalRatio - 1.00) < 0.001; // Allowing a small margin for floating-point errors

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "The sum of fund ratios must be 1%. Current sum: " + "%.2f".formatted(totalRatio))
                    .addConstraintViolation();
        }

        return isValid;
    }
}
