package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import com.flinkboot.kafka.api.properties.source.TopicPartitionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaTopicSubscriptionRule")
class KafkaTopicSubscriptionRuleTest {

    private final KafkaTopicSubscriptionRule rule = new KafkaTopicSubscriptionRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail on topics when none of topics, topic-pattern, or partitions is specified")
    void shouldFailWhenNoneSpecified() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, null, null, null, null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "topics",
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail on topic-pattern when both topics and topic-pattern are specified")
    void shouldFailWhenTopicsAndPatternSpecified() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), "t.*", null, null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "topic-pattern",
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail on partitions when partitions is combined with topics")
    void shouldFailWhenTopicsAndPartitionsSpecified() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null,
            List.of(new TopicPartitionProperties("t1", 0)), null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "partitions",
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when exactly one subscription mode is specified")
    void shouldPassWhenExactlyOneSpecified() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
