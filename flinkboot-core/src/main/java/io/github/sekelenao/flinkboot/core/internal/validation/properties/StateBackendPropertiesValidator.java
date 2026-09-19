package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import java.util.Objects;

import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates cross-field invariants for {@link StateBackendProperties}.
 */
public final class StateBackendPropertiesValidator {

    private StateBackendPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(StateBackendProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");
        var typeOpt = properties.type();
        var customClassOpt = properties.customClass();
        var isCustom = typeOpt.isPresent() && typeOpt.get() == StateBackendType.CUSTOM;
        var hasCustomClass = customClassOpt.isPresent();

        if (isCustom && !hasCustomClass) {
            return PropertiesValidator.reject(
                context,
                "customClass",
                "custom-class must be specified when state backend type is CUSTOM"
            );
        }

        if (!isCustom && hasCustomClass) {
            return PropertiesValidator.reject(
                context,
                "customClass",
                "custom-class can only be specified when state backend type is CUSTOM"
            );
        }

        return true;
    }
}
