package com.flinkboot.kafka.internal.validation.properties.sink;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates consistency between {@code delivery-guarantee} and {@code transactional-id-prefix} in {@link KafkaSinkProperties}.
 */
public final class KafkaTransactionalIdPrefixRule implements ValidationRule<KafkaSinkProperties> {

    @Override
    public Optional<PropertyViolation> check(KafkaSinkProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var guarantee = properties.deliveryGuarantee();
        if (guarantee == null) {
            return Optional.empty();
        }
        var isExactlyOnce = guarantee == KafkaDeliveryGuarantee.EXACTLY_ONCE;
        var hasPrefix = properties.transactionalIdPrefix().isPresent();

        if (isExactlyOnce && !hasPrefix) {
            return Optional.of(new PropertyViolation(
                "transactional-id-prefix",
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            ));
        }
        if (!isExactlyOnce && hasPrefix) {
            return Optional.of(new PropertyViolation(
                "transactional-id-prefix",
                "transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE"
            ));
        }
        return Optional.empty();
    }
}
