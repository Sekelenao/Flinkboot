package com.flinkboot.kafka.internal.validation.properties;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link KafkaSourceProperties}.
 */
public final class KafkaSourcePropertiesValidator {

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
        return validSubscription && validBoundedness;
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
        var isBounded = properties.boundedness().orElse(KafkaBoundedness.UNBOUNDED) == KafkaBoundedness.BOUNDED;
        if (isBounded && properties.stoppingOffsets().isEmpty()) {
            return reject("stopping-offsets", "stopping-offsets is required when boundedness is BOUNDED");
        }
        return true;
    }

    private boolean reject(String property, String message) {
        return PropertiesValidator.reject(context, property, message);
    }
}
