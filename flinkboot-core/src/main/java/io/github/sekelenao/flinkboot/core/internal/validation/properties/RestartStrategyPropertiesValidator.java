package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyType;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link RestartStrategyProperties}.
 */
public final class RestartStrategyPropertiesValidator {

    private final RestartStrategyProperties properties;
    private final ConstraintValidatorContext context;

    private RestartStrategyPropertiesValidator(RestartStrategyProperties properties, ConstraintValidatorContext context) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    public static boolean validate(RestartStrategyProperties properties, ConstraintValidatorContext context) {
        return new RestartStrategyPropertiesValidator(properties, context).execute();
    }

    private boolean execute() {
        var type = properties.type().orElse(RestartStrategyType.FALLBACK);
        switch (type) {
            case FALLBACK: case NO_RESTART: return validateNoSubConfiguration(type);
            case FIXED_DELAY: return validateFixedDelay();
            case FAILURE_RATE: return validateFailureRate();
            case EXPONENTIAL_DELAY: return validateExponentialDelay();
            default: throw new IllegalStateException("Unhandled RestartStrategyType: " + type);
        }
    }

    private boolean validateNoSubConfiguration(RestartStrategyType type) {
        var fixedDelayPresent = properties.fixedDelay().isPresent();
        var failureRatePresent = properties.failureRate().isPresent();
        var exponentialDelayPresent = properties.exponentialDelay().isPresent();
        if (fixedDelayPresent || failureRatePresent || exponentialDelayPresent) {
            if (properties.type().isEmpty()) {
                return reject(
                    "type",
                    "restart strategy type is required when configuring a sub-block (fixed-delay, failure-rate, exponential-delay)"
                );
            }
            return reject(
                "type",
                "No sub-configuration (fixed-delay, failure-rate, exponential-delay) must be specified when restart strategy type is " + type
            );
        }
        return true;
    }

    private boolean validateFixedDelay() {
        if (properties.failureRate().isPresent() || properties.exponentialDelay().isPresent()) {
            return reject(
                "type",
                "Cannot specify failure-rate or exponential-delay when restart strategy type is FIXED_DELAY"
            );
        }
        return true;
    }

    private boolean validateFailureRate() {
        if (properties.fixedDelay().isPresent() || properties.exponentialDelay().isPresent()) {
            return reject(
                "type",
                "Cannot specify fixed-delay or exponential-delay when restart strategy type is FAILURE_RATE"
            );
        }
        return true;
    }

    private boolean validateExponentialDelay() {
        if (properties.fixedDelay().isPresent() || properties.failureRate().isPresent()) {
            return reject(
                "type",
                "Cannot specify fixed-delay or failure-rate when restart strategy type is EXPONENTIAL_DELAY"
            );
        }

        var expoOpt = properties.exponentialDelay();
        if (expoOpt.isPresent()) {
            var expo = expoOpt.get();
            var initial = expo.initialBackoff();
            var max = expo.maxBackoff();
            if (initial.isPresent() && max.isPresent() && max.get().compareTo(initial.get()) < 0) {
                return reject(
                    "exponentialDelay",
                    "max-backoff cannot be smaller than initial-backoff in exponential-delay restart strategy"
                );
            }
        }
        return true;
    }

    private boolean reject(String property, String message) {
        return PropertiesValidator.reject(context, property, message);
    }
}
