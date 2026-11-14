package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that at least one of {@code savepoint-path} or {@code directory} is specified.
 */
public final class SavepointRestoreLocationRule implements ValidationRule<SavepointRestoreProperties> {

    @Override
    public Optional<PropertyViolation> check(SavepointRestoreProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.savepointPath().isEmpty() && properties.directory().isEmpty()) {
            return Optional.of(new PropertyViolation(
                "savepoint-path",
                "at least one of savepoint-path or directory must be specified"
            ));
        }
        return Optional.empty();
    }
}
