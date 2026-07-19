package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaSinkConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSinkConfigurationException;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

import java.util.function.Consumer;

public final class DeliveryGuaranteeMapper {

    private DeliveryGuaranteeMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> Consumer<KafkaSinkBuilder<T>> map(KafkaSinkConfiguration configuration) {
        var optGuarantee = configuration.deliveryGuarantee();
        var prefix = configuration.transactionalIdPrefix();

        if (prefix.isPresent() && (optGuarantee.isEmpty() || optGuarantee.get() != KafkaDeliveryGuarantee.EXACTLY_ONCE)) {
            throw new InvalidKafkaSinkConfigurationException("transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE");
        }

        if (optGuarantee.isPresent() && optGuarantee.get() == KafkaDeliveryGuarantee.EXACTLY_ONCE && prefix.isEmpty()) {
            throw new InvalidKafkaSinkConfigurationException("transactional-id-prefix is required and cannot be empty when delivery-guarantee is EXACTLY_ONCE");
        }

        if (optGuarantee.isEmpty() && prefix.isEmpty()) {
            return builder -> {};
        }

        return builder -> {
            optGuarantee.ifPresent(g -> builder.setDeliveryGuarantee(g.deliveryGuarantee()));
            prefix.ifPresent(builder::setTransactionalIdPrefix);
        };
    }
}
