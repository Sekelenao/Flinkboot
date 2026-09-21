package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.api.validation.ValidConfiguration;
import io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Jakarta constraint validator bridging {@link ValidConfiguration} to {@link ValidatableProperties#validate(ConstraintValidatorContext)}.
 * Also provides shared helper utilities for binding violations to property nodes.
 */
public final class PropertiesValidator implements ConstraintValidator<ValidConfiguration, ValidatableProperties> {

    @Override
    public boolean isValid(ValidatableProperties value, ConstraintValidatorContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (value == null) {
            return true;
        }
        return value.validate(context);
    }

    /**
     * Binds a constraint violation with the given template message to a specific property node.
     *
     * @param context  the constraint validator context
     * @param property the target property name
     * @param message  the violation error message
     * @return always {@code false} to indicate validation failure
     */
    public static boolean reject(ConstraintValidatorContext context, String property, String message) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(property, "property must not be null");
        Objects.requireNonNull(message, "message must not be null");

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(property)
               .addConstraintViolation();
        return false;
    }
}
