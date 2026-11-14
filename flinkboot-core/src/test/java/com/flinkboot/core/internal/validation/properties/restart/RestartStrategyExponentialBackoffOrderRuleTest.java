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
    @DisplayName("Should pass when max-backoff equals or exceeds initial-backoff")
    void shouldPassWhenMaxBackoffEqualsInitialBackoff() {
        var props = new RestartStrategyProperties(
            RestartStrategyType.EXPONENTIAL_DELAY,
            null,
            null,
            new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(10), null, null, null, null)
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
