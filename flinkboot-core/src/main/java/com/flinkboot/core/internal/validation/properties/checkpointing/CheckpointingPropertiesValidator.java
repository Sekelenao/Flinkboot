package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link CheckpointingProperties}.
 */
public final class CheckpointingPropertiesValidator {

    private static final List<ValidationRule<CheckpointingProperties>> RULES = List.of(
        new CheckpointingIntervalRule(),
        new CheckpointingStorageUriRule(),
        new CheckpointingConcurrencyRule(),
        new CheckpointingAlignedTimeoutRule()
    );

    private CheckpointingPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(CheckpointingProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
