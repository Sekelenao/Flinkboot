package io.github.sekelenao.flinkboot.kafka.api.sink;

import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("KafkaSinkFactory")
class KafkaSinkFactoryTest {

    private static final KafkaRecordSerializationSchema<String> TEST_SCHEMA = (element, context, timestamp) ->
        new ProducerRecord<>("my-topic", element.getBytes());

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor")
    class Supply {

        @Test
        @DisplayName("Should successfully build KafkaSink and KafkaSinkBuilder with default configuration")
        void shouldBuildKafkaSink() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                Map.of("client.id", "test-client")
            );

            assertAll(
                () -> assertNotNull(KafkaSinkFactory.supplyFor(config, TEST_SCHEMA)),
                () -> assertNotNull(KafkaSinkFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @Test
        @DisplayName("Should successfully build with EXACTLY_ONCE and transactional ID prefix")
        void shouldBuildWithExactlyOnce() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "my-transaction-prefix",
                null
            );

            assertAll(
                () -> assertNotNull(KafkaSinkFactory.supplyFor(config, TEST_SCHEMA)),
                () -> assertNotNull(KafkaSinkFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @ParameterizedTest
        @EnumSource(value = KafkaDeliveryGuarantee.class, names = {"NONE", "AT_LEAST_ONCE"})
        @DisplayName("Should successfully build with NONE and AT_LEAST_ONCE delivery guarantees")
        void shouldBuildWithOtherGuarantees(KafkaDeliveryGuarantee guarantee) {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                guarantee,
                null,
                null
            );
            assertNotNull(KafkaSinkFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            assertAll(
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyFor((KafkaSinkProperties) null, TEST_SCHEMA));
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyFor(config, null));
                    assertEquals("serializationSchema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyBuilderFor((KafkaSinkProperties) null, TEST_SCHEMA));
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyBuilderFor(config, null));
                    assertEquals("serializationSchema must not be null", ex.getMessage());
                }
            );
        }
    }
}
