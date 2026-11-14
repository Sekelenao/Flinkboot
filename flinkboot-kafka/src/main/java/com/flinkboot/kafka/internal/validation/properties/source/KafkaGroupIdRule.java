package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code group-id} is specified when offset strategies require a consumer group.
 */
public final class KafkaGroupIdRule implements ValidationRule<KafkaSourceProperties> {

    @Override
    public Optional<PropertyViolation> check(KafkaSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.groupId().isPresent()) {
            return Optional.empty();
        }
        var startingRequiresGroupId = requiresGroupId(properties.startingOffsets());
        var stoppingRequiresGroupId = properties.stoppingOffsets()
            .map(KafkaGroupIdRule::requiresGroupId)
            .orElse(false);
        if (startingRequiresGroupId || stoppingRequiresGroupId) {
            return Optional.of(new PropertyViolation(
                "group-id",
                "group-id is required when starting-offsets or stopping-offsets uses a COMMITTED strategy"
            ));
        }
        return Optional.empty();
    }

    private static boolean requiresGroupId(KafkaOffsetProperties offsets) {
        if (offsets == null || offsets.strategy() == null) {
            return false;
        }
        return offsets.strategy().requiresGroupId();
    }
}
