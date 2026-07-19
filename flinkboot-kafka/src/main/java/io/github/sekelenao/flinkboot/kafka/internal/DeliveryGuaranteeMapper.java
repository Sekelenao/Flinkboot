package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSinkConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSinkConfigurationException;
import org.apache.flink.connector.base.DeliveryGuarantee;

public final class DeliveryGuaranteeMapper {

    private DeliveryGuaranteeMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static DeliveryGuarantee map(KafkaSinkConfiguration configuration) {
        var guarantee = configuration.deliveryGuarantee().orElse(KafkaDeliveryGuarantee.AT_LEAST_ONCE);
        if (guarantee == KafkaDeliveryGuarantee.EXACTLY_ONCE) {
            var prefix = configuration.transactionalIdPrefix();
            if (prefix.isEmpty()) {
                throw new InvalidKafkaSinkConfigurationException("transactional-id-prefix is required and cannot be empty when delivery-guarantee is EXACTLY_ONCE");
            }
        }
        return guarantee.deliveryGuarantee();
    }
}
