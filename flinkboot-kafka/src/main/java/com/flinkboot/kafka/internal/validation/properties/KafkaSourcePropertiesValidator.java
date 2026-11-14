package com.flinkboot.kafka.internal.validation.properties;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Validates cross-field invariants for {@link KafkaSourceProperties}.
 */
public final class KafkaSourcePropertiesValidator {

    private static final Set<KafkaOffsetInitializer> COMMITTED_STRATEGIES = EnumSet.of(
        KafkaOffsetInitializer.COMMITTED,
        KafkaOffsetInitializer.COMMITTED_EARLIEST,
        KafkaOffsetInitializer.COMMITTED_LATEST
    );

    private final KafkaSourceProperties properties;

    private final ConstraintValidatorContext context;

    private KafkaSourcePropertiesValidator(KafkaSourceProperties properties, ConstraintValidatorContext context) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    public static boolean validate(KafkaSourceProperties properties, ConstraintValidatorContext context) {
        return new KafkaSourcePropertiesValidator(properties, context).execute();
    }

    private boolean execute() {
        var validSubscription = validateTopicSubscription();
        var validBoundedness = validateBoundedness();
        var validGroupId = validateGroupId();
        return validSubscription && validBoundedness && validGroupId;
    }

    private boolean validateTopicSubscription() {
        var hasTopics = !properties.topics().isEmpty();
        var hasPattern = properties.topicPattern().isPresent();
        var hasPartitions = !properties.partitions().isEmpty();

        int count = (hasTopics ? 1 : 0) + (hasPattern ? 1 : 0) + (hasPartitions ? 1 : 0);
        if (count != 1) {
            var property = count == 0 ? "topics" : (hasPartitions ? "partitions" : "topic-pattern");
            return reject(property, "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified");
        }
        return true;
    }

    private boolean validateBoundedness() {
        if (properties.stoppingOffsets().isPresent() && properties.boundedness().isEmpty()) {
            return reject("boundedness", "boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured");
        }
        var isBounded = properties.boundedness().orElse(KafkaBoundedness.UNBOUNDED) == KafkaBoundedness.BOUNDED;
        if (isBounded && properties.stoppingOffsets().isEmpty()) {
            return reject("stopping-offsets", "stopping-offsets is required when boundedness is BOUNDED");
        }
        return true;
    }

    private boolean validateGroupId() {
        if (properties.groupId().isPresent()) {
            return true;
        }
        var usesCommittedStarting = isCommittedStrategy(properties.startingOffsets());
        var usesCommittedStopping = properties.stoppingOffsets()
            .map(this::isCommittedStrategy)
            .orElse(false);
        if (usesCommittedStarting || usesCommittedStopping) {
            return reject("group-id", "group-id is required when starting-offsets or stopping-offsets uses a COMMITTED strategy");
        }
        return true;
    }

    private boolean isCommittedStrategy(KafkaOffsetProperties offsets) {
        if (offsets == null || offsets.strategy() == null) {
            return false;
        }
        return COMMITTED_STRATEGIES.contains(offsets.strategy());
    }

    private boolean reject(String property, String message) {
        return PropertiesValidator.reject(context, property, message);
    }
}
