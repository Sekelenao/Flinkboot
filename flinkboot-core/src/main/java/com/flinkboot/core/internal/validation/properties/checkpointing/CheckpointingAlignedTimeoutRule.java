package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code unaligned-checkpoints} is explicitly {@code true}
 * whenever {@code aligned-checkpoint-timeout} is configured.
 */
public final class CheckpointingAlignedTimeoutRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        boolean unalignedEnabled = properties.unalignedCheckpoints()
            .filter(Boolean::booleanValue)
            .isPresent();
        if (properties.alignedCheckpointTimeout().isPresent() && !unalignedEnabled) {
            return Optional.of(new PropertyViolation(
                "aligned-checkpoint-timeout",
                "aligned-checkpoint-timeout requires unaligned-checkpoints to be true"
            ));
        }
        return Optional.empty();
    }
}
