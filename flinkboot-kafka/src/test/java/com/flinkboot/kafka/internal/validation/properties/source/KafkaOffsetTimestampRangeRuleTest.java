package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaOffsetTimestampRangeRule")
class KafkaOffsetTimestampRangeRuleTest {

    private final KafkaOffsetTimestampRangeRule rule = new KafkaOffsetTimestampRangeRule();

    private static KafkaSourceProperties sourceWithOffsets(
        KafkaOffsetProperties starting,
        KafkaOffsetProperties stopping
    ) {
        return new KafkaSourceProperties(
            null,
            List.of("localhost:9092"),
            null,
            null,
            List.of("t1"),
            null,
            null,
            starting,
            stopping == null ? null : KafkaBoundedness.BOUNDED,
            stopping,
            null
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should pass when starting-offsets is null")
    void shouldPassWhenStartingOffsetsIsNull() {
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        assertTrue(rule.check(sourceWithOffsets(null, stopping)).isEmpty());
    }

    @Test
    @DisplayName("Should pass when stopping-offsets is absent")
    void shouldPassWhenStoppingOffsetsIsAbsent() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, null)).isEmpty());
    }

    @Test
    @DisplayName("Should pass when starting-offsets strategy is not TIMESTAMP")
    void shouldPassWhenStartingStrategyIsNotTimestamp() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @Test
    @DisplayName("Should pass when stopping-offsets strategy is not TIMESTAMP")
    void shouldPassWhenStoppingStrategyIsNotTimestamp() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @Test
    @DisplayName("Should pass when starting-offsets timestamp is missing")
    void shouldPassWhenStartingTimestampIsMissing() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, null, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @Test
    @DisplayName("Should pass when stopping-offsets timestamp is missing")
    void shouldPassWhenStoppingTimestampIsMissing() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, null, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "-1, 100",
        "100, -1"
    })
    @DisplayName("Should pass when either timestamp is negative (delegated to field validation)")
    void shouldPassWhenEitherTimestampIsNegative(long start, long stop) {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, start, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, stop, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "100, 100",
        "100, 200"
    })
    @DisplayName("Should pass when starting-offsets timestamp is less than or equal to stopping-offsets timestamp")
    void shouldPassWhenStartingTimestampIsLessThanOrEqualToStoppingTimestamp(long start, long stop) {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, start, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, stop, null, null);
        assertTrue(rule.check(sourceWithOffsets(starting, stopping)).isEmpty());
    }

    @Test
    @DisplayName("Should fail when starting-offsets timestamp is greater than stopping-offsets timestamp")
    void shouldFailWhenStartingTimestampIsGreaterThanStoppingTimestamp() {
        var starting = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 200L, null, null);
        var stopping = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "stopping-offsets",
                "stopping-offsets.timestamp must be greater than or equal to starting-offsets.timestamp"
            )),
            rule.check(sourceWithOffsets(starting, stopping))
        );
    }
}
