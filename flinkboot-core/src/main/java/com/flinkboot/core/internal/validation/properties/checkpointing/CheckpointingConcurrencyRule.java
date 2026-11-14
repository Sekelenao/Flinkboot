package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code max-concurrent-checkpoints} is 1 when {@code min-pause-between-checkpoints} is positive.
 */
public final class CheckpointingConcurrencyRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var minPause = properties.minPauseBetweenCheckpoints();
        var maxConcurrent = properties.maxConcurrentCheckpoints();
        if (minPause.isPresent() && !minPause.get().isZero()
                && maxConcurrent.isPresent() && maxConcurrent.getAsInt() > 1) {
            return Optional.of(new PropertyViolation(
                "max-concurrent-checkpoints",
                "max-concurrent-checkpoints must be 1 when min-pause-between-checkpoints is greater than 0"
            ));
        }
        return Optional.empty();
    }
}
