package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
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

@DisplayName("RestartStrategyExponentialBackoffPresenceRule")
class RestartStrategyExponentialBackoffPresenceRuleTest {

    private final RestartStrategyExponentialBackoffPresenceRule rule =
        new RestartStrategyExponentialBackoffPresenceRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when only initial-backoff is specified")
    void shouldFailWhenOnlyInitialBackoffIsSpecified() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(1), null, null, null, null, null)
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "exponential-delay",
                "both initial-backoff and max-backoff must be specified together in exponential-delay restart strategy"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when only max-backoff is specified")
    void shouldFailWhenOnlyMaxBackoffIsSpecified() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(null, Duration.ofSeconds(10), null, null, null, null)
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "exponential-delay",
                "both initial-backoff and max-backoff must be specified together in exponential-delay restart strategy"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when both initial-backoff and max-backoff are specified")
    void shouldPassWhenBothAreSpecified() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofSeconds(10), null, null, null, null)
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when both initial-backoff and max-backoff are absent")
    void shouldPassWhenBothAreAbsent() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(null, null, 2.0, null, null, null)
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when type is null or not EXPONENTIAL_DELAY")
    void shouldPassWhenNotExponentialDelay() {
        var withNullType = new RestartStrategyProperties(
            null,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(1), null, null, null, null, null)
        );
        var withFixedDelay = new RestartStrategyProperties(
            RestartStrategyType.FIXED_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(1), null, null, null, null, null)
        );

        assertTrue(rule.check(withNullType).isEmpty());
        assertTrue(rule.check(withFixedDelay).isEmpty());
    }

    @Test
    @DisplayName("Should pass when exponential-delay block is absent")
    void shouldPassWhenExponentialDelayIsAbsent() {
        var props = new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
