package com.app.folioman.portfolio.web.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SumOfRatiosValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSumOfRatios {
    String message() default "Sum of fund ratios must equal 100";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
