package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Objects;

/**
 * Validates cross-field invariants for {@link CheckpointingProperties}.
 */
public final class CheckpointingPropertiesValidator {

    private static final List<ValidationRule<CheckpointingProperties>> RULES = List.of(
        new CheckpointingIntervalRule(),
        new CheckpointingStorageUriRule(),
        new CheckpointingConcurrencyRule(),
        new CheckpointingAlignedTimeoutRule(),
        new CheckpointingUnalignedModeRule()
    );

    private CheckpointingPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(CheckpointingProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");

        if (!properties.enabled().orElse(true)) {
            return true;
        }

        return PropertiesValidator.compute(properties, context, RULES);
    }
}
