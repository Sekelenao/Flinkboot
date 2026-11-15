package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code incremental} is not enabled when {@code type} is {@link StateBackendType#HASHMAP}.
 */
public final class StateBackendIncrementalRule implements ValidationRule<StateBackendProperties> {

    @Override
    public Optional<PropertyViolation> check(StateBackendProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        boolean isHashMap = properties.type()
            .filter(type -> type == StateBackendType.HASHMAP)
            .isPresent();
        boolean incrementalEnabled = properties.incremental()
            .filter(Boolean::booleanValue)
            .isPresent();
        if (isHashMap && incrementalEnabled) {
            return Optional.of(new PropertyViolation(
                "incremental",
                "incremental cannot be true when state backend type is HASHMAP"
            ));
        }
        return Optional.empty();
    }
}
