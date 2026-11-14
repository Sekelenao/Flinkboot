package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates consistency between {@code boundedness} and {@code stopping-offsets} in {@link KafkaSourceProperties}.
 */
public final class KafkaBoundednessRule implements ValidationRule<KafkaSourceProperties> {

    @Override
    public Optional<PropertyViolation> check(KafkaSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.stoppingOffsets().isPresent() && properties.boundedness().isEmpty()) {
            return Optional.of(new PropertyViolation(
                "boundedness",
                "boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured"
            ));
        }
        var isBounded = properties.boundedness().orElse(KafkaBoundedness.UNBOUNDED) == KafkaBoundedness.BOUNDED;
        if (isBounded && properties.stoppingOffsets().isEmpty()) {
            return Optional.of(new PropertyViolation(
                "stopping-offsets",
                "stopping-offsets is required when boundedness is BOUNDED"
            ));
        }
        return Optional.empty();
    }
}
