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

@DisplayName("RestartStrategyExponentialBackoffOrderRule")
class RestartStrategyExponentialBackoffOrderRuleTest {

    private final RestartStrategyExponentialBackoffOrderRule rule =
        new RestartStrategyExponentialBackoffOrderRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when max-backoff is smaller than initial-backoff")
    void shouldFailWhenMaxBackoffSmallerThanInitialBackoff() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(5), null, null, null, null)
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "exponential-delay",
                "max-backoff cannot be smaller than initial-backoff in exponential-delay restart strategy"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when max-backoff equals initial-backoff")
    void shouldPassWhenMaxBackoffEqualsInitialBackoff() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(10), null, null, null, null)
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when max-backoff exceeds initial-backoff")
    void shouldPassWhenMaxBackoffExceedsInitialBackoff() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofSeconds(10), null, null, null, null)
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
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(5), null, null, null, null)
        );
        var withFixedDelay = new RestartStrategyProperties(
            RestartStrategyType.FIXED_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(5), null, null, null, null)
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

    @Test
    @DisplayName("Should pass when initial-backoff or max-backoff is absent")
    void shouldPassWhenBackoffIsAbsent() {
        var missingInitial = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(null, Duration.ofSeconds(10), null, null, null, null)
        );
        var missingMax = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), null, null, null, null, null)
        );

        assertTrue(rule.check(missingInitial).isEmpty());
        assertTrue(rule.check(missingMax).isEmpty());
    }
}
