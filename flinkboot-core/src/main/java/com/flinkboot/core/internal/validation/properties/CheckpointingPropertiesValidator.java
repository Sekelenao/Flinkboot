package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.properties.checkpointing.CheckpointStorageType;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link CheckpointingProperties}.
 */
public final class CheckpointingPropertiesValidator {

    private CheckpointingPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(CheckpointingProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");
        boolean enabled = properties.enabled().orElse(true);
        if (enabled && properties.interval().isEmpty()) {
            return PropertiesValidator.reject(
                context,
                "interval",
                "interval must be specified when checkpointing is enabled"
            );
        }

        var storage = properties.storage();
        if (storage.isPresent() && storage.get() == CheckpointStorageType.FILESYSTEM) {
            var storageUri = properties.storageUri();
            if (storageUri.isEmpty() || storageUri.get().isBlank()) {
                return PropertiesValidator.reject(
                    context,
                    "storage-uri",
                    "storage-uri must be specified when checkpoint storage is FILESYSTEM"
                );
            }
        }

        var minPause = properties.minPauseBetweenCheckpoints();
        var maxConcurrent = properties.maxConcurrentCheckpoints();

        if (minPause.isPresent() && !minPause.get().isZero() && maxConcurrent.isPresent() && maxConcurrent.getAsInt() > 1) {
            return PropertiesValidator.reject(
                context,
                "max-concurrent-checkpoints",
                "max-concurrent-checkpoints must be 1 when min-pause-between-checkpoints is greater than 0"
            );
        }

        return true;
    }

}
