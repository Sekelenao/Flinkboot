package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingIntervalRule")
class CheckpointingIntervalRuleTest {

    private final CheckpointingIntervalRule rule = new CheckpointingIntervalRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true})
    @DisplayName("Should fail when checkpointing is enabled or default without an interval")
    void shouldFailWhenEnabledWithoutInterval(Boolean enabled) {
        var props = new CheckpointingProperties(
            enabled, null, null, null, null, null, null, null, null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation("interval", "interval must be specified when checkpointing is enabled")),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when checkpointing is disabled without an interval")
    void shouldPassWhenDisabledWithoutInterval() {
        var props = new CheckpointingProperties(
            false, null, null, null, null, null, null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when checkpointing is enabled with an interval")
    void shouldPassWhenEnabledWithInterval() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
