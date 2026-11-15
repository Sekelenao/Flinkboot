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

@DisplayName("CheckpointingAlignedTimeoutRule")
class CheckpointingAlignedTimeoutRuleTest {

    private final CheckpointingAlignedTimeoutRule rule = new CheckpointingAlignedTimeoutRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false})
    @DisplayName("Should fail when unaligned-checkpoints is false or null and aligned-checkpoint-timeout is set")
    void shouldFailWhenUnalignedNotEnabledAndAlignedTimeoutSet(Boolean unalignedCheckpoints) {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, unalignedCheckpoints, Duration.ofSeconds(5), null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "aligned-checkpoint-timeout",
                "aligned-checkpoint-timeout requires unaligned-checkpoints to be true"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when unaligned-checkpoints is true and aligned-checkpoint-timeout is set")
    void shouldPassWhenUnalignedEnabledAndAlignedTimeoutSet() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, true, Duration.ofSeconds(5), null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false, true})
    @DisplayName("Should pass when aligned-checkpoint-timeout is null regardless of unaligned-checkpoints")
    void shouldPassWhenAlignedTimeoutIsNull(Boolean unalignedCheckpoints) {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, unalignedCheckpoints, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
