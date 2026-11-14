package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RestartStrategySubBlockRule")
class RestartStrategySubBlockRuleTest {

    private final RestartStrategySubBlockRule rule = new RestartStrategySubBlockRule(
        RestartStrategyType.FIXED_DELAY,
        "fixed-delay",
        properties -> properties.fixedDelay().isPresent()
    );

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when sub-block is present and type is absent")
    void shouldFailWhenSubBlockPresentAndTypeAbsent() {
        var props = new RestartStrategyProperties(
            null,
            new FixedDelayRestartProperties(3, Duration.ofSeconds(10)),
            null,
            null
        );

        assertEquals(
            Optional.of(new PropertyViolation("type", "restart strategy type is required when configuring fixed-delay")),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when sub-block is present for a different type")
    void shouldFailWhenSubBlockPresentForDifferentType() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.NO_RESTART,
            new FixedDelayRestartProperties(3, Duration.ofSeconds(10)),
            null,
            null
        );

        assertEquals(
            Optional.of(new PropertyViolation("type", "fixed-delay cannot be specified when restart strategy type is NO_RESTART")),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when sub-block is present for matching type")
    void shouldPassWhenSubBlockPresentForMatchingType() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.FIXED_DELAY,
            new FixedDelayRestartProperties(3, Duration.ofSeconds(10)),
            null,
            null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when sub-block is absent")
    void shouldPassWhenSubBlockAbsent() {
        var props = new RestartStrategyProperties(RestartStrategyType.NO_RESTART, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
