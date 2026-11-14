package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingConcurrencyRule")
class CheckpointingConcurrencyRuleTest {

    private final CheckpointingConcurrencyRule rule = new CheckpointingConcurrencyRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when min-pause-between-checkpoints is positive and max-concurrent-checkpoints > 1")
    void shouldFailWhenMinPausePositiveAndMaxConcurrentGreaterThanOne() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), 2,
            null, null, null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "max-concurrent-checkpoints",
                "max-concurrent-checkpoints must be 1 when min-pause-between-checkpoints is greater than 0"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when min-pause-between-checkpoints is zero and max-concurrent-checkpoints > 1")
    void shouldPassWhenMinPauseZero() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, Duration.ZERO, 2,
            null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when min-pause-between-checkpoints is positive and max-concurrent-checkpoints is 1")
    void shouldPassWhenMaxConcurrentIsOne() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), 1,
            null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
