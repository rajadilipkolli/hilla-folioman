package com.app.folioman.portfolio.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.PastOrPresent;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {})
@Target({
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.PARAMETER,
    ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@PastOrPresent
@ReportAsSingleViolation
public @interface ValidPastOrPresent {
    String message() default "Date should be past or today";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
