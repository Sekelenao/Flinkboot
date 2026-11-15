package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code allow-non-restored-state} and {@code restore-mode} are not configured
 * when {@code savepoint-path} is absent.
 */
public final class SavepointRestoreOptionsRule implements ValidationRule<SavepointRestoreProperties> {

    @Override
    public Optional<PropertyViolation> check(SavepointRestoreProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.savepointPath().isPresent()) {
            return Optional.empty();
        }
        if (properties.allowNonRestoredState().isPresent()) {
            return Optional.of(new PropertyViolation(
                "allow-non-restored-state",
                "allow-non-restored-state cannot be set when savepoint-path is absent"
            ));
        }
        if (properties.restoreMode().isPresent()) {
            return Optional.of(new PropertyViolation(
                "restore-mode",
                "restore-mode cannot be set when savepoint-path is absent"
            ));
        }
        return Optional.empty();
    }
}
