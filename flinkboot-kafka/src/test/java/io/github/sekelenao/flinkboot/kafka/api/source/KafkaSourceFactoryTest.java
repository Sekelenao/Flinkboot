package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSourcePropertiesException;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("KafkaSourceFactory")
class KafkaSourceFactoryTest {

    private static final KafkaRecordDeserializationSchema<String> TEST_SCHEMA = new KafkaRecordDeserializationSchema<>() {
        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> rcrd, Collector<String> out) { /* Do nothing */ }

        @Override
        public TypeInformation<String> getProducedType() {
            return Types.STRING;
        }
    };

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = KafkaSourceFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Test
    @DisplayName("OffsetInitializerMapper private constructor should throw AssertionError")
    void testOffsetInitializerMapperConstructorIsPrivate() throws Exception {
        var constructor = OffsetInitializerMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (Topic List)")
    class SupplyForTopicList {

        @Test
        @DisplayName("Should successfully build KafkaSource and KafkaSourceBuilder from topic list config")
        void shouldBuildKafkaSource() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of("client.id", "test-client")
            );

            assertAll(
                () -> assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA)),
                () -> assertNotNull(KafkaSourceFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @Test
        @DisplayName("Should successfully build with valid TIMESTAMP offset config")
        void shouldBuildWithTimestamp() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                1689717600000L,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build with valid OFFSETS offset config")
        void shouldBuildWithPartitionOffsets() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetProperties("test-topic", 0, 100L)),
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build with LATEST, COMMITTED, COMMITTED_EARLIEST, COMMITTED_LATEST offset configs")
        void shouldBuildWithOtherOffsetInitializers() {
            for (var initializer : List.of(KafkaOffsetInitializer.LATEST, KafkaOffsetInitializer.COMMITTED, KafkaOffsetInitializer.COMMITTED_EARLIEST, KafkaOffsetInitializer.COMMITTED_LATEST)) {
                var config = new KafkaSourceProperties(
                    "my-source",
                    List.of("localhost:9092"),
                    "test-group",
                    List.of("test-topic"),
                    null,
                    initializer,
                    null,
                    null,
                    null
                );
                assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
            }
        }
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (Topic Pattern)")
    class SupplyForTopicPattern {

        @Test
        @DisplayName("Should successfully build KafkaSource and KafkaSourceBuilder from topic pattern config")
        void shouldBuildKafkaSource() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                null,
                "test-.*",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                Map.of("client.id", "test-client")
            );

            assertAll(
                () -> assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA)),
                () -> assertNotNull(KafkaSourceFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @Test
        @DisplayName("Should successfully build with valid TIMESTAMP offset config")
        void shouldBuildWithTimestamp() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                null,
                "test-.*",
                KafkaOffsetInitializer.TIMESTAMP,
                1689717600000L,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build with valid OFFSETS offset config")
        void shouldBuildWithPartitionOffsets() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                null,
                "test-.*",
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetProperties("test-topic", 0, 100L)),
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }
    }

    @Nested
    @DisplayName("Null Checks")
    class NullChecks {

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor(null, TEST_SCHEMA));
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor(config, null));
                    assertEquals("schema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor(null, TEST_SCHEMA));
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor(config, null));
                    assertEquals("schema must not be null", ex.getMessage());
                }
            );
        }
    }
}
