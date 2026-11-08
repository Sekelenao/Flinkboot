package com.flinkboot.kafka.api.properties.sink;

import org.apache.flink.connector.base.DeliveryGuarantee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("KafkaDeliveryGuarantee")
class KafkaDeliveryGuaranteeTest {

    @Nested
    @DisplayName("DeliveryGuarantee Resolution")
    class DeliveryGuaranteeResolution {

        @ParameterizedTest
        @CsvSource({
            "EXACTLY_ONCE,  EXACTLY_ONCE",
            "AT_LEAST_ONCE, AT_LEAST_ONCE",
            "NONE,          NONE"
        })
        @DisplayName("Should configure correct Flink DeliveryGuarantee mapping")
        void shouldMapToFlinkDeliveryGuarantee(
            KafkaDeliveryGuarantee flinkbootGuarantee,
            DeliveryGuarantee expectedFlinkGuarantee
        ) {
            assertEquals(expectedFlinkGuarantee, flinkbootGuarantee.deliveryGuarantee());
        }

        @ParameterizedTest
        @EnumSource(KafkaDeliveryGuarantee.class)
        @DisplayName("Should ensure delivery guarantee is non-null for all enum values")
        void shouldEnsureNonNullDeliveryGuarantee(KafkaDeliveryGuarantee guarantee) {
            assertNotNull(guarantee.deliveryGuarantee());
        }
    }
}
