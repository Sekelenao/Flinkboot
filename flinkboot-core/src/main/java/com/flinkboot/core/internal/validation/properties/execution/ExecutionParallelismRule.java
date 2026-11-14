package com.flinkboot.core.internal.validation.properties.execution;

import com.flinkboot.core.api.properties.execution.ExecutionProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code parallelism} does not exceed {@code max-parallelism}.
 */
public final class ExecutionParallelismRule implements ValidationRule<ExecutionProperties> {

    @Override
    public Optional<PropertyViolation> check(ExecutionProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var parallelism = properties.parallelism();
        var maxParallelism = properties.maxParallelism();
        if (parallelism.isEmpty() || maxParallelism.isEmpty()) {
            return Optional.empty();
        }
        var current = parallelism.getAsInt();
        var max = maxParallelism.getAsInt();
        if (current > max) {
            return Optional.of(new PropertyViolation(
                "parallelism",
                "parallelism (" + current + ") cannot exceed max-parallelism (" + max + ")"
            ));
        }
        return Optional.empty();
    }
}
