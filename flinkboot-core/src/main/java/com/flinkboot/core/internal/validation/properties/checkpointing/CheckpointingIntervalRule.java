package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that a checkpoint interval is specified when checkpointing is enabled.
 */
public final class CheckpointingIntervalRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        boolean enabled = properties.enabled().orElse(true);
        if (enabled && properties.interval().isEmpty()) {
            return Optional.of(new PropertyViolation(
                "interval",
                "interval must be specified when checkpointing is enabled"
            ));
        }
        return Optional.empty();
    }
}
