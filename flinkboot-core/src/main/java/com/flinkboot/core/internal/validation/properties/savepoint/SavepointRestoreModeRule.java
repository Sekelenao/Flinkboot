package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code restore-mode} is not configured when {@code savepoint-path} is absent.
 */
public final class SavepointRestoreModeRule implements ValidationRule<SavepointRestoreProperties> {

    @Override
    public Optional<PropertyViolation> check(SavepointRestoreProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.savepointPath().isEmpty() && properties.restoreMode().isPresent()) {
            return Optional.of(new PropertyViolation(
                "restore-mode",
                "restore-mode cannot be set when savepoint-path is absent"
            ));
        }
        return Optional.empty();
    }
}
