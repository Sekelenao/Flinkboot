package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaBoundednessRule")
class KafkaBoundednessRuleTest {

    private final KafkaBoundednessRule rule = new KafkaBoundednessRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when stopping-offsets is specified without boundedness")
    void shouldFailWhenStoppingOffsetsWithoutBoundedness() {
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null, null, stopping, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "boundedness",
                "boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when boundedness is BOUNDED without stopping-offsets")
    void shouldFailWhenBoundedWithoutStoppingOffsets() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            KafkaBoundedness.BOUNDED, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "stopping-offsets",
                "stopping-offsets is required when boundedness is BOUNDED"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when boundedness is BOUNDED with stopping-offsets")
    void shouldPassWhenBoundedWithStoppingOffsets() {
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            KafkaBoundedness.BOUNDED, stopping, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when boundedness and stopping-offsets are both absent")
    void shouldPassWhenBothAbsent() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when boundedness is UNBOUNDED without stopping-offsets")
    void shouldPassWhenUnboundedWithoutStoppingOffsets() {
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            KafkaBoundedness.UNBOUNDED, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when boundedness is UNBOUNDED with stopping-offsets")
    void shouldPassWhenUnboundedWithStoppingOffsets() {
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            KafkaBoundedness.UNBOUNDED, stopping, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
