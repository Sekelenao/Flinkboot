package com.flinkboot.fluss.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link FlussSourceProperties}.
 */
public final class FlussSourcePropertiesValidator {

    private static final List<ValidationRule<FlussSourceProperties>> RULES = List.of(
        new FlussStartupTimestampRule()
    );

    private FlussSourcePropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(FlussSourceProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
