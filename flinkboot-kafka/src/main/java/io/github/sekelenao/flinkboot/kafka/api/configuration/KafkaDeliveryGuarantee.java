package io.github.sekelenao.flinkboot.kafka.api.configuration;

import org.apache.flink.connector.base.DeliveryGuarantee;

public enum KafkaDeliveryGuarantee {
    EXACTLY_ONCE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.EXACTLY_ONCE;
        }
    },
    AT_LEAST_ONCE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.AT_LEAST_ONCE;
        }
    },
    NONE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.NONE;
        }
    };

    public abstract DeliveryGuarantee deliveryGuarantee();
}
