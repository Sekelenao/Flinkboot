package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaSinkConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSinkConfigurationException;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

import java.util.function.Consumer;

public final class DeliveryGuaranteeCustomizer {

    private DeliveryGuaranteeCustomizer() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> Consumer<KafkaSinkBuilder<T>> supplyFor(KafkaSinkConfiguration configuration) {
        var optGuarantee = configuration.deliveryGuarantee();
        var optPrefix = configuration.transactionalIdPrefix();

        boolean exactlyOnce = optGuarantee.map(guarantee -> guarantee == KafkaDeliveryGuarantee.EXACTLY_ONCE).orElse(false);
        boolean hasPrefix = optPrefix.isPresent();

        if (exactlyOnce && !hasPrefix) {
            throw new InvalidKafkaSinkConfigurationException("transactional-id-prefix is required and cannot be empty when delivery-guarantee is EXACTLY_ONCE");
        }

        if (!exactlyOnce && hasPrefix) {
            throw new InvalidKafkaSinkConfigurationException("transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE");
        }

        return builder -> {
            optGuarantee.ifPresent(guarantee -> builder.setDeliveryGuarantee(guarantee.deliveryGuarantee()));
            optPrefix.ifPresent(builder::setTransactionalIdPrefix);
        };
    }
}
