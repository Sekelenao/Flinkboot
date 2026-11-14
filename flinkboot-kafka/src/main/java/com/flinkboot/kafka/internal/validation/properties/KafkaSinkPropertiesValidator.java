package com.flinkboot.kafka.internal.validation.properties;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validates cross-field invariants for {@link KafkaSinkProperties}.
 */
public final class KafkaSinkPropertiesValidator {

    private KafkaSinkPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(KafkaSinkProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");
        var guarantee = properties.deliveryGuarantee();
        if (guarantee == null) {
            return true;
        }
        var isExactlyOnce = guarantee == KafkaDeliveryGuarantee.EXACTLY_ONCE;
        var hasPrefix = properties.transactionalIdPrefix().isPresent();

        if (isExactlyOnce && !hasPrefix) {
            return PropertiesValidator.reject(
                context,
                "transactional-id-prefix",
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            );
        }

        if (!isExactlyOnce && hasPrefix) {
            return PropertiesValidator.reject(
                context,
                "transactional-id-prefix",
                "transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE"
            );
        }

        return true;
    }
}
