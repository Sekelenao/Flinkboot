package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code initial-backoff} and {@code max-backoff} are specified together for exponential-delay restart strategy.
 */
public final class RestartStrategyExponentialBackoffPresenceRule implements ValidationRule<RestartStrategyProperties> {

    @Override
    public Optional<PropertyViolation> check(RestartStrategyProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (properties.type().orElse(RestartStrategyType.FALLBACK) != RestartStrategyType.EXPONENTIAL_DELAY) {
            return Optional.empty();
        }
        var expoOpt = properties.exponentialDelay();
        if (expoOpt.isEmpty()) {
            return Optional.empty();
        }
        var expo = expoOpt.get();
        if (expo.initialBackoff().isPresent() != expo.maxBackoff().isPresent()) {
            return Optional.of(new PropertyViolation(
                "exponential-delay",
                "both initial-backoff and max-backoff must be specified together in exponential-delay restart strategy"
            ));
        }
        return Optional.empty();
    }
}
