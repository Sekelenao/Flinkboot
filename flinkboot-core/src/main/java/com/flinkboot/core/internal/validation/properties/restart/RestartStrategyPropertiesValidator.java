package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link RestartStrategyProperties}.
 */
public final class RestartStrategyPropertiesValidator {

    private static final List<ValidationRule<RestartStrategyProperties>> RULES = List.of(
        new RestartStrategySubBlockRule(
            RestartStrategyType.FIXED_DELAY,
            "fixed-delay",
            properties -> properties.fixedDelay().isPresent()
        ),
        new RestartStrategySubBlockRule(
            RestartStrategyType.FAILURE_RATE,
            "failure-rate",
            properties -> properties.failureRate().isPresent()
        ),
        new RestartStrategySubBlockRule(
            RestartStrategyType.EXPONENTIAL_DELAY,
            "exponential-delay",
            properties -> properties.exponentialDelay().isPresent()
        ),
        new RestartStrategyExponentialBackoffPresenceRule(),
        new RestartStrategyExponentialBackoffOrderRule()
    );

    private RestartStrategyPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(RestartStrategyProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
