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

@DisplayName("KafkaGroupIdRule")
class KafkaGroupIdRuleTest {

    private final KafkaGroupIdRule rule = new KafkaGroupIdRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when starting-offsets uses COMMITTED without group-id")
    void shouldFailWhenCommittedOffsetsWithoutGroupId() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, starting, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "group-id",
                "group-id is required when starting-offsets or stopping-offsets uses a COMMITTED strategy"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when stopping-offsets uses COMMITTED without group-id")
    void shouldFailWhenStoppingCommittedWithoutGroupId() {
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null,
            KafkaBoundedness.BOUNDED, stopping, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "group-id",
                "group-id is required when starting-offsets or stopping-offsets uses a COMMITTED strategy"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when group-id is specified with COMMITTED")
    void shouldPassWhenGroupIdIsSpecified() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null, null);
        var props = new KafkaSourceProperties(
            null, List.of("localhost:9092"), "my-group", null, List.of("t1"), null, null, starting,
            KafkaBoundedness.UNBOUNDED, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when group-id is absent and offset strategies do not require a group-id")
    void shouldPassWhenGroupIdAbsentAndNotRequired() {
        var withNullOffsets = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null, null, null, null, null
        );
        var withNullStrategy = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null,
            new KafkaOffsetProperties(null, null, null, null), null, null, null
        );
        var withEarliest = new KafkaSourceProperties(
            null, List.of("localhost:9092"), null, null, List.of("t1"), null, null,
            new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null), null, null, null
        );

        assertTrue(rule.check(withNullOffsets).isEmpty());
        assertTrue(rule.check(withNullStrategy).isEmpty());
        assertTrue(rule.check(withEarliest).isEmpty());
    }
}
