package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import java.util.Objects;

import io.github.sekelenao.flinkboot.core.internal.validation.properties.PropertiesValidator;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.ConstraintValidatorContext;

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
        var guarantee = properties.deliveryGuarantee().orElse(null);
        var isExactlyOnce = guarantee == KafkaDeliveryGuarantee.EXACTLY_ONCE;
        var hasPrefix = properties.transactionalIdPrefix().isPresent();

        if (isExactlyOnce && !hasPrefix) {
            return PropertiesValidator.reject(
                context,
                "transactionalIdPrefix",
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            );
        }

        if (!isExactlyOnce && hasPrefix) {
            return PropertiesValidator.reject(
                context,
                "transactionalIdPrefix",
                "transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE"
            );
        }

        return true;
    }
}
