package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingUnalignedModeRule")
class CheckpointingUnalignedModeRuleTest {

    private final CheckpointingUnalignedModeRule rule = new CheckpointingUnalignedModeRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when mode is AT_LEAST_ONCE and unaligned-checkpoints is true")
    void shouldFailWhenModeIsAtLeastOnceAndUnalignedEnabled() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), CheckpointingMode.AT_LEAST_ONCE, null, null, null, null, true, null, null, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "unaligned-checkpoints",
                "unaligned-checkpoints cannot be true when mode is AT_LEAST_ONCE"
            )),
            rule.check(props)
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false})
    @DisplayName("Should pass when mode is AT_LEAST_ONCE and unaligned-checkpoints is false or null")
    void shouldPassWhenModeIsAtLeastOnceAndUnalignedDisabledOrNull(Boolean unalignedCheckpoints) {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), CheckpointingMode.AT_LEAST_ONCE, null, null, null, null, unalignedCheckpoints, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = CheckpointingMode.class, names = "EXACTLY_ONCE")
    @DisplayName("Should pass when mode is EXACTLY_ONCE or null and unaligned-checkpoints is true")
    void shouldPassWhenModeIsExactlyOnceOrNullAndUnalignedEnabled(CheckpointingMode mode) {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), mode, null, null, null, null, true, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when both mode and unaligned-checkpoints are null")
    void shouldPassWhenBothAreNull() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null, null, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
