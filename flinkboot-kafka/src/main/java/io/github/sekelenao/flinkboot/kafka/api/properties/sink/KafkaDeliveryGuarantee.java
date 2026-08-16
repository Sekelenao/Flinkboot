package io.github.sekelenao.flinkboot.kafka.api.properties.sink;

import org.apache.flink.connector.base.DeliveryGuarantee;

/**
 * Delivery consistency guarantee semantics for Kafka sink producers.
 */
public enum KafkaDeliveryGuarantee {
    /**
     * Exactly-once semantic guarantee using Kafka transactions.
     */
    EXACTLY_ONCE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.EXACTLY_ONCE;
        }
    },
    /**
     * At-least-once semantic guarantee with checkpointed message acknowledgments.
     */
    AT_LEAST_ONCE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.AT_LEAST_ONCE;
        }
    },
    /**
     * No delivery guarantee (at-most-once / best effort fire-and-forget).
     */
    NONE {
        @Override
        public DeliveryGuarantee deliveryGuarantee() {
            return DeliveryGuarantee.NONE;
        }
    };

    /**
     * Returns the corresponding Flink {@link DeliveryGuarantee}.
     *
     * @return the {@link DeliveryGuarantee} enum value
     */
    public abstract DeliveryGuarantee deliveryGuarantee();
}
