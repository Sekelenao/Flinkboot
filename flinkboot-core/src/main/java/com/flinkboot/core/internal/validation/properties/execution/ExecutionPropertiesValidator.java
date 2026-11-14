package com.flinkboot.core.internal.validation.properties.execution;

import com.flinkboot.core.api.properties.execution.ExecutionProperties;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link ExecutionProperties}.
 */
public final class ExecutionPropertiesValidator {

    private static final List<ValidationRule<ExecutionProperties>> RULES = List.of(
        new ExecutionParallelismRule()
    );

    private ExecutionPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(ExecutionProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
