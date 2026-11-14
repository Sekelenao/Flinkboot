package com.flinkboot.kafka.internal.validation.properties.offset;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link KafkaOffsetProperties}.
 */
public final class KafkaOffsetPropertiesValidator {

    private static final List<ValidationRule<KafkaOffsetProperties>> RULES = List.of(
        new KafkaOffsetStrategyFieldRule(
            KafkaOffsetInitializer.TIMESTAMP,
            "timestamp",
            properties -> properties.timestamp().isPresent()
        ),
        new KafkaOffsetStrategyFieldRule(
            KafkaOffsetInitializer.OFFSETS,
            "partitions",
            properties -> !properties.partitions().isEmpty()
        ),
        new KafkaOffsetStrategyFieldRule(
            KafkaOffsetInitializer.OFFSETS,
            "reset-strategy",
            properties -> properties.resetStrategy().isPresent()
        )
    );

    private KafkaOffsetPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(KafkaOffsetProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
