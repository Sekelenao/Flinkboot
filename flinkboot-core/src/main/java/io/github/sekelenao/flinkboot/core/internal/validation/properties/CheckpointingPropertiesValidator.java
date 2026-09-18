package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import java.util.Objects;

import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import jakarta.validation.ConstraintValidatorContext;

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
        return true;
    }

}
