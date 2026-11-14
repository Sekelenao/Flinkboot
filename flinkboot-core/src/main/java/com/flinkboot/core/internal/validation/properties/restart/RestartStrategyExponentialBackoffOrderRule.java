package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates that {@code max-backoff} is not smaller than {@code initial-backoff} for exponential-delay restart strategy.
 */
public final class RestartStrategyExponentialBackoffOrderRule implements ValidationRule<RestartStrategyProperties> {

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
        var initial = expo.initialBackoff();
        var max = expo.maxBackoff();
        if (initial.isPresent() && max.isPresent() && max.get().compareTo(initial.get()) < 0) {
            return Optional.of(new PropertyViolation(
                "exponential-delay",
                "max-backoff cannot be smaller than initial-backoff in exponential-delay restart strategy"
            ));
        }
        return Optional.empty();
    }
}
