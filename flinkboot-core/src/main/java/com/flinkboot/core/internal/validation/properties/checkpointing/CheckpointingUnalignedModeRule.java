package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code unaligned-checkpoints} is not enabled when
 * {@code mode} is {@link CheckpointingMode#AT_LEAST_ONCE}.
 */
public final class CheckpointingUnalignedModeRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        boolean atLeastOnce = properties.mode()
            .filter(mode -> mode == CheckpointingMode.AT_LEAST_ONCE)
            .isPresent();
        boolean unalignedEnabled = properties.unalignedCheckpoints()
            .filter(Boolean::booleanValue)
            .isPresent();
        if (atLeastOnce && unalignedEnabled) {
            return Optional.of(new PropertyViolation(
                "unaligned-checkpoints",
                "unaligned-checkpoints cannot be true when mode is AT_LEAST_ONCE"
            ));
        }
        return Optional.empty();
    }
}
