package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link SavepointRestoreProperties}.
 */
public final class SavepointRestorePropertiesValidator {

    private static final List<ValidationRule<SavepointRestoreProperties>> RULES = List.of(
        new SavepointRestoreLocationRule(),
        new SavepointAllowNonRestoredStateRule(),
        new SavepointRestoreModeRule()
    );

    private SavepointRestorePropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(SavepointRestoreProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
