package com.flinkboot.core.internal.validation.properties;

import java.util.Optional;

/**
 * Composable cross-field validation rule for configuration properties DTOs.
 *
 * @param <T> the properties DTO type
 */
@FunctionalInterface
public interface ValidationRule<T> {

    /**
     * Evaluates this rule against the given properties instance.
     *
     * @param properties the properties instance to validate (must not be null)
     * @return an {@link Optional} containing the {@link PropertyViolation} if the rule fails,
     * or {@link Optional#empty()} if valid
     */
    Optional<PropertyViolation> check(T properties);
}
