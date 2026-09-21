package io.github.sekelenao.flinkboot.core.api.validation;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Contract for configuration properties classes requiring cross-field validation.
 * <p>
 * Implementing classes automatically inherit the {@link ValidConfiguration} constraint annotation.
 * During Jakarta Bean Validation, the {@link #validate(ConstraintValidatorContext)} method is invoked,
 * allowing interdependent fields to be checked and fine-grained violations to be bound directly
 * to specific property nodes.
 */
@ValidConfiguration
public interface ValidatableProperties {

    /**
     * Validates cross-field invariants for this configuration properties instance.
     *
     * @param context the constraint validator context used to register customized violations
     * @return {@code true} if the configuration is valid, {@code false} if validation failed
     */
    boolean validate(ConstraintValidatorContext context);
}
