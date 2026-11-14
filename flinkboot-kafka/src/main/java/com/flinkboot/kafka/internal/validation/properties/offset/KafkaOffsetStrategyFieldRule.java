package com.flinkboot.kafka.internal.validation.properties.offset;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Validates that a field in {@link KafkaOffsetProperties} is present if and only if {@code strategy} matches {@code expectedStrategy}.
 */
public final class KafkaOffsetStrategyFieldRule implements ValidationRule<KafkaOffsetProperties> {

    private final KafkaOffsetInitializer expectedStrategy;
    private final String propertyName;
    private final Predicate<KafkaOffsetProperties> isPresent;

    /**
     * Creates a new strategy field rule.
     *
     * @param expectedStrategy the offset strategy requiring this field
     * @param propertyName     the YAML property name
     * @param isPresent        predicate checking whether the field is configured
     */
    public KafkaOffsetStrategyFieldRule(
        KafkaOffsetInitializer expectedStrategy,
        String propertyName,
        Predicate<KafkaOffsetProperties> isPresent
    ) {
        this.expectedStrategy = Objects.requireNonNull(expectedStrategy, "expectedStrategy must not be null");
        this.propertyName = Objects.requireNonNull(propertyName, "propertyName must not be null");
        this.isPresent = Objects.requireNonNull(isPresent, "isPresent must not be null");
    }

    @Override
    public Optional<PropertyViolation> check(KafkaOffsetProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var currentStrategy = properties.strategy();
        if (currentStrategy == null) {
            return Optional.empty();
        }
        var present = isPresent.test(properties);
        if (currentStrategy == expectedStrategy && !present) {
            return Optional.of(new PropertyViolation(
                propertyName,
                propertyName + " is required when strategy is " + currentStrategy
            ));
        }
        if (currentStrategy != expectedStrategy && present) {
            return Optional.of(new PropertyViolation(
                propertyName,
                propertyName + " must not be specified when strategy is " + currentStrategy
            ));
        }
        return Optional.empty();
    }
}
