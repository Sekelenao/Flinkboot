package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that exactly one of {@code topics}, {@code topic-pattern}, or {@code partitions} is specified.
 */
public final class KafkaTopicSubscriptionRule implements ValidationRule<KafkaSourceProperties> {

    @Override
    public Optional<PropertyViolation> check(KafkaSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var hasTopics = !properties.topics().isEmpty();
        var hasPattern = properties.topicPattern().isPresent();
        var hasPartitions = !properties.partitions().isEmpty();

        int count = (hasTopics ? 1 : 0) + (hasPattern ? 1 : 0) + (hasPartitions ? 1 : 0);
        if (count != 1) {
            var property = count == 0 ? "topics" : (hasPartitions ? "partitions" : "topic-pattern");
            return Optional.of(new PropertyViolation(
                property,
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            ));
        }
        return Optional.empty();
    }
}
