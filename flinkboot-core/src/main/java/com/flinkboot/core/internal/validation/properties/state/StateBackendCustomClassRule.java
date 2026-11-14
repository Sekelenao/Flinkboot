package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates consistency between {@code type} and {@code custom-class} in {@link StateBackendProperties}.
 */
public final class StateBackendCustomClassRule implements ValidationRule<StateBackendProperties> {

    @Override
    public Optional<PropertyViolation> check(StateBackendProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var typeOpt = properties.type();
        var isCustom = typeOpt.isPresent() && typeOpt.get() == StateBackendType.CUSTOM;
        var hasCustomClass = properties.customClass().isPresent();

        if (isCustom && !hasCustomClass) {
            return Optional.of(new PropertyViolation(
                "custom-class",
                "custom-class must be specified when state backend type is CUSTOM"
            ));
        }
        if (!isCustom && hasCustomClass) {
            return Optional.of(new PropertyViolation(
                "custom-class",
                "custom-class can only be specified when state backend type is CUSTOM"
            ));
        }
        return Optional.empty();
    }
}
