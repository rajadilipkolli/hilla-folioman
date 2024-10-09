package com.app.folioman.portfolio.web.validator;

import com.app.folioman.portfolio.models.request.Fund;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class SumOfRatiosValidator implements ConstraintValidator<ValidSumOfRatios, List<Fund>> {

    @Override
    public boolean isValid(List<Fund> funds, ConstraintValidatorContext context) {
        if (funds == null || funds.isEmpty()) {
            // @NotEmpty annotation should handle this case separately
            return true;
        }

        double totalRatio = funds.stream().mapToDouble(Fund::ratio).sum();

        // Check if the sum of ratios is exactly 100
        return Math.abs(totalRatio - 100.0) < 0.0001; // Allowing a small margin for floating-point errors
    }
}
