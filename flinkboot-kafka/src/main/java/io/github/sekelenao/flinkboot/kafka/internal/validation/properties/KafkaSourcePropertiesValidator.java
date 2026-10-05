package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.internal.validation.properties.PropertiesValidator;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
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
        if (hasTopics && hasPattern) {
            return reject("topic-pattern", "Cannot configure both 'topics' and 'topic-pattern'");
        }
        if (!hasTopics && !hasPattern) {
            return reject("topics", "Either 'topics' or 'topic-pattern' must be specified");
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
