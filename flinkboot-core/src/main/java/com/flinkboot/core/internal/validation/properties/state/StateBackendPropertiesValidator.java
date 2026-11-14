package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link StateBackendProperties}.
 */
public final class StateBackendPropertiesValidator {

    private static final List<ValidationRule<StateBackendProperties>> RULES = List.of(
        new StateBackendCustomClassRule()
    );

    private StateBackendPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(StateBackendProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
