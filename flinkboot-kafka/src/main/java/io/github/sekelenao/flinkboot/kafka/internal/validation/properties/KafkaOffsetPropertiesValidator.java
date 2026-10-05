package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.internal.validation.properties.PropertiesValidator;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link KafkaOffsetProperties}.
 */
public final class KafkaOffsetPropertiesValidator {

    private static final String TIMESTAMP_PROPERTY = "timestamp";

    private static final String PARTITIONS_PROPERTY = "partitions";

    private final KafkaOffsetProperties properties;

    private final ConstraintValidatorContext context;

    private KafkaOffsetPropertiesValidator(KafkaOffsetProperties properties, ConstraintValidatorContext context) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    public static boolean validate(KafkaOffsetProperties properties, ConstraintValidatorContext context) {
        return new KafkaOffsetPropertiesValidator(properties, context).execute();
    }

    private boolean execute() {
        if (properties.strategy() == null) {
            return true;
        }
        var isTimestampValid = validateField(
            KafkaOffsetInitializer.TIMESTAMP, properties.timestamp().isPresent(), TIMESTAMP_PROPERTY
        );
        var arePartitionsValid = validateField(
            KafkaOffsetInitializer.OFFSETS, !properties.partitions().isEmpty(), PARTITIONS_PROPERTY
        );
        return isTimestampValid && arePartitionsValid;
    }

    private boolean validateField(KafkaOffsetInitializer expectedStrategy, boolean isPresent, String propertyName) {
        var currentStrategy = properties.strategy();
        if (currentStrategy == expectedStrategy) {
            if (!isPresent) {
                return reject(propertyName, propertyName + " is required when strategy is " + currentStrategy);
            }
        } else if (isPresent) {
            return reject(propertyName, propertyName + " must not be specified when strategy is " + currentStrategy);
        }
        return true;
    }

    private boolean reject(String propertyName, String message) {
        return PropertiesValidator.reject(context, propertyName, message);
    }
}
