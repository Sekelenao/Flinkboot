package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicListConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicPatternConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.configuration.TopicPartitionConfiguration;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
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

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (Topic List)")
    class SupplyForTopicList {

        @Test
        @DisplayName("Should successfully build KafkaSource and KafkaSourceBuilder from topic list config")
        void shouldBuildKafkaSource() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
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
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
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
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionConfiguration("test-topic", 0, 100L)),
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when TIMESTAMP is used but timestamp is missing")
        void shouldThrowExceptionWhenTimestampMissing() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                KafkaOffsetInitializer.TIMESTAMP,
                null,
                null,
                null
            );

            assertThrows(IllegalArgumentException.class, () -> KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when OFFSETS is used but partition offsets are missing")
        void shouldThrowExceptionWhenOffsetsMissing() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                KafkaOffsetInitializer.OFFSETS,
                null,
                null,
                null
            );

            assertThrows(IllegalArgumentException.class, () -> KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor((KafkaSourceTopicListConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor(config, null)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor((KafkaSourceTopicListConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor(config, null))
            );
        }
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (Topic Pattern)")
    class SupplyForTopicPattern {

        @Test
        @DisplayName("Should successfully build KafkaSource and KafkaSourceBuilder from topic pattern config")
        void shouldBuildKafkaSource() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "test-group",
                Pattern.compile("test-.*"),
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
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "test-group",
                Pattern.compile("test-.*"),
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
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "test-group",
                Pattern.compile("test-.*"),
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionConfiguration("test-topic", 0, 100L)),
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "test-group",
                Pattern.compile("test-.*"),
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor((KafkaSourceTopicPatternConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyFor(config, null)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor((KafkaSourceTopicPatternConfiguration) null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor(config, null))
            );
        }
    }
}
