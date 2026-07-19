package io.github.sekelenao.flinkboot.kafka.api.sink;

import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaSinkConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSinkConfigurationException;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("KafkaSinkFactory")
class KafkaSinkFactoryTest {

    private static final KafkaRecordSerializationSchema<String> TEST_SCHEMA = (element, context, timestamp) ->
        new ProducerRecord<>("my-topic", element.getBytes());

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor")
    class SupplyTests {

        @Test
        @DisplayName("Should successfully build KafkaSink and KafkaSinkBuilder with default configuration")
        void shouldBuildKafkaSink() {
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                null,
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
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "my-transaction-prefix",
                null
            );

            assertNotNull(KafkaSinkFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSinkConfigurationException when EXACTLY_ONCE is requested but prefix is missing")
        void shouldThrowExceptionWhenPrefixIsMissing() {
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                null,
                null
            );

            assertThrows(
                InvalidKafkaSinkConfigurationException.class,
                () -> KafkaSinkFactory.supplyFor(config, TEST_SCHEMA)
            );
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSinkConfigurationException when AT_LEAST_ONCE is used but prefix is provided")
        void shouldThrowExceptionWhenPrefixIsProvidedWithAtLeastOnce() {
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                "some-prefix",
                null
            );

            assertThrows(
                InvalidKafkaSinkConfigurationException.class,
                () -> KafkaSinkFactory.supplyFor(config, TEST_SCHEMA)
            );
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSinkConfigurationException when default delivery guarantee is used but prefix is provided")
        void shouldThrowExceptionWhenPrefixIsProvidedWithDefaultGuarantee() {
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                null,
                "some-prefix",
                null
            );

            assertThrows(
                InvalidKafkaSinkConfigurationException.class,
                () -> KafkaSinkFactory.supplyFor(config, TEST_SCHEMA)
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var config = new KafkaSinkConfiguration(
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyFor((KafkaSinkConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyFor(config, null)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyBuilderFor((KafkaSinkConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSinkFactory.supplyBuilderFor(config, null))
            );
        }
    }
}
