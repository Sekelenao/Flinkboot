package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Validates that a restart strategy sub-configuration block is only present when {@code type} matches {@code expectedType}.
 */
public final class RestartStrategySubBlockRule implements ValidationRule<RestartStrategyProperties> {

    private final RestartStrategyType expectedType;
    private final String blockName;
    private final Predicate<RestartStrategyProperties> isPresent;

    /**
     * Creates a new sub-block rule.
     *
     * @param expectedType the restart strategy type that allows this sub-block
     * @param blockName    the YAML sub-block name
     * @param isPresent    predicate checking whether the sub-block is configured
     */
    public RestartStrategySubBlockRule(
        RestartStrategyType expectedType,
        String blockName,
        Predicate<RestartStrategyProperties> isPresent
    ) {
        this.expectedType = Objects.requireNonNull(expectedType, "expectedType must not be null");
        this.blockName = Objects.requireNonNull(blockName, "blockName must not be null");
        this.isPresent = Objects.requireNonNull(isPresent, "isPresent must not be null");
    }

    @Override
    public Optional<PropertyViolation> check(RestartStrategyProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        if (!isPresent.test(properties)) {
            return Optional.empty();
        }
        var typeOpt = properties.type();
        if (typeOpt.isEmpty()) {
            return Optional.of(new PropertyViolation(
                "type",
                "restart strategy type is required when configuring " + blockName
            ));
        }
        if (typeOpt.get() != expectedType) {
            return Optional.of(new PropertyViolation(
                "type",
                blockName + " cannot be specified when restart strategy type is " + typeOpt.get()
            ));
        }
        return Optional.empty();
    }
}
