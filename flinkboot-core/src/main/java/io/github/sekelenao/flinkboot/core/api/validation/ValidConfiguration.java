package io.github.sekelenao.flinkboot.core.api.validation;

import io.github.sekelenao.flinkboot.core.internal.validation.properties.PropertiesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint annotation triggering cross-field validation for configuration properties classes.
 * <p>
 * This annotation can be placed directly on configuration properties classes or is automatically
 * inherited by classes implementing {@link ValidatableProperties}.
 */
@Documented
@Constraint(validatedBy = PropertiesValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidConfiguration {

    /**
     * Error message template when cross-field validation fails.
     *
     * @return the error message template
     */
    String message() default "Invalid configuration properties";

    /**
     * Validation groups targeted by this constraint.
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload associated with this constraint.
     *
     * @return the payload classes
     */
    Class<? extends Payload>[] payload() default {};
}
