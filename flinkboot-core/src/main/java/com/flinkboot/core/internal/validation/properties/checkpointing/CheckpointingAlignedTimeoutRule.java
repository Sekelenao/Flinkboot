package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code aligned-checkpoint-timeout} is not configured when
 * {@code unaligned-checkpoints} is explicitly disabled.
 */
public final class CheckpointingAlignedTimeoutRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        boolean unalignedDisabled = properties.unalignedCheckpoints()
            .filter(unaligned -> !unaligned)
            .isPresent();
        if (unalignedDisabled && properties.alignedCheckpointTimeout().isPresent()) {
            return Optional.of(new PropertyViolation(
                "aligned-checkpoint-timeout",
                "aligned-checkpoint-timeout cannot be set when unaligned-checkpoints is false"
            ));
        }
        return Optional.empty();
    }
}
