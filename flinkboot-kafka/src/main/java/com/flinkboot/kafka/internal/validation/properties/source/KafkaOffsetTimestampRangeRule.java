package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code starting-offsets.timestamp} is less than or equal to {@code stopping-offsets.timestamp}
 * when both offset configurations use {@link KafkaOffsetInitializer#TIMESTAMP}.
 */
public final class KafkaOffsetTimestampRangeRule implements ValidationRule<KafkaSourceProperties> {

    @Override
    public Optional<PropertyViolation> check(KafkaSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var starting = properties.startingOffsets();
        var stopping = properties.stoppingOffsets().orElse(null);
        if (starting == null || stopping == null) {
            return Optional.empty();
        }
        if (starting.strategy() != KafkaOffsetInitializer.TIMESTAMP || stopping.strategy() != KafkaOffsetInitializer.TIMESTAMP) {
            return Optional.empty();
        }
        var startingTimestamp = starting.timestamp();
        var stoppingTimestamp = stopping.timestamp();
        if (startingTimestamp.isEmpty() || stoppingTimestamp.isEmpty()) {
            return Optional.empty();
        }
        var start = startingTimestamp.getAsLong();
        var stop = stoppingTimestamp.getAsLong();
        if (start < 0 || stop < 0) {
            return Optional.empty();
        }
        if (start > stop) {
            return Optional.of(new PropertyViolation(
                "stopping-offsets",
                "stopping-offsets.timestamp must be greater than or equal to starting-offsets.timestamp"
            ));
        }
        return Optional.empty();
    }
}
