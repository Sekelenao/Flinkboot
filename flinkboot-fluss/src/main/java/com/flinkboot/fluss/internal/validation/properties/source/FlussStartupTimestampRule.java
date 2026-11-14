package com.flinkboot.fluss.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import com.flinkboot.fluss.api.properties.source.FlussStartupMode;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates consistency between {@code startup-mode} and {@code startup-timestamp} in {@link FlussSourceProperties}.
 */
public final class FlussStartupTimestampRule implements ValidationRule<FlussSourceProperties> {

    @Override
    public Optional<PropertyViolation> check(FlussSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var mode = properties.startupMode();
        if (mode == null) {
            return Optional.empty();
        }
        var timestamp = properties.startupTimestamp();
        if (mode == FlussStartupMode.TIMESTAMP && timestamp.isEmpty()) {
            return Optional.of(new PropertyViolation(
                "startup-timestamp",
                "startup-timestamp is required when startup-mode is TIMESTAMP"
            ));
        }
        if (mode != FlussStartupMode.TIMESTAMP && timestamp.isPresent()) {
            return Optional.of(new PropertyViolation(
                "startup-timestamp",
                "startup-timestamp must not be specified when startup-mode is " + mode
            ));
        }
        return Optional.empty();
    }
}
