package com.flinkboot.kafka.internal.validation.properties.offset;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaOffsetStrategyFieldRule")
class KafkaOffsetStrategyFieldRuleTest {

    private final KafkaOffsetStrategyFieldRule rule = new KafkaOffsetStrategyFieldRule(
        KafkaOffsetInitializer.TIMESTAMP,
        "timestamp",
        properties -> properties.timestamp().isPresent()
    );

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when strategy matches expectedStrategy and field is absent")
    void shouldFailWhenExpectedStrategyAndFieldAbsent() {
        var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, null, null, null);

        assertEquals(
            Optional.of(new PropertyViolation("timestamp", "timestamp is required when strategy is TIMESTAMP")),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when strategy differs from expectedStrategy and field is present")
    void shouldFailWhenOtherStrategyAndFieldPresent() {
        var props = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, 12345L, null, null);

        assertEquals(
            Optional.of(new PropertyViolation("timestamp", "timestamp must not be specified when strategy is EARLIEST")),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when strategy matches expectedStrategy and field is present")
    void shouldPassWhenExpectedStrategyAndFieldPresent() {
        var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 12345L, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
