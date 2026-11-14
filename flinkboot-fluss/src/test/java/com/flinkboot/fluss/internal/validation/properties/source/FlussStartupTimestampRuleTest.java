package com.flinkboot.fluss.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import com.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlussStartupTimestampRule")
class FlussStartupTimestampRuleTest {

    private final FlussStartupTimestampRule rule = new FlussStartupTimestampRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when startup-mode is TIMESTAMP without startup-timestamp")
    void shouldFailWhenTimestampModeWithoutTimestamp() {
        var props = new FlussSourceProperties(
            null, List.of("localhost:9123"), "db", "tbl", FlussStartupMode.TIMESTAMP, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "startup-timestamp",
                "startup-timestamp is required when startup-mode is TIMESTAMP"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when startup-mode is not TIMESTAMP with startup-timestamp")
    void shouldFailWhenNonTimestampModeWithTimestamp() {
        var props = new FlussSourceProperties(
            null, List.of("localhost:9123"), "db", "tbl", FlussStartupMode.LATEST, 123456L, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "startup-timestamp",
                "startup-timestamp must not be specified when startup-mode is LATEST"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when startup-mode is TIMESTAMP with startup-timestamp")
    void shouldPassWhenTimestampModeWithTimestamp() {
        var props = new FlussSourceProperties(
            null, List.of("localhost:9123"), "db", "tbl", FlussStartupMode.TIMESTAMP, 123456L, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
