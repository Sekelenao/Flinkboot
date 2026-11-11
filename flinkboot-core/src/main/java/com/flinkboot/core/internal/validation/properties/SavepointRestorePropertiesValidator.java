package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link SavepointRestoreProperties}.
 */
public final class SavepointRestorePropertiesValidator {

    private SavepointRestorePropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(SavepointRestoreProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");

        if (properties.savepointPath().isEmpty() && properties.directory().isEmpty()) {
            return PropertiesValidator.reject(
                context,
                "savepoint-path",
                "at least one of savepoint-path or directory must be specified"
            );
        }

        return true;
    }
}
