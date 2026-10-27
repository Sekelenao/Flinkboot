package com.flinkboot.kafka.api.source;

import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetResetStrategy;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import com.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
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

    private static final KafkaOffsetProperties DEFAULT_STARTING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null);

    private static final KafkaOffsetProperties DEFAULT_STOPPING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);

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
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
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
            var timestampOffsets = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                timestampOffsets,
                null,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build with valid OFFSETS offset config")
        void shouldBuildWithPartitionOffsets() {
            var partitionOffsets = new KafkaOffsetProperties(
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetProperties("test-topic", 0, 100L)),
                KafkaOffsetResetStrategy.EARLIEST
            );
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                partitionOffsets,
                null,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build bounded source with stopping-offsets and verify BOUNDED boundedness")
        void shouldBuildBoundedSourceWithStoppingOffsets() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                null
            );

            var source = KafkaSourceFactory.supplyFor(config, TEST_SCHEMA);

            assertAll(
                () -> assertNotNull(source),
                () -> assertEquals(Boundedness.BOUNDED, source.getBoundedness()),
                () -> assertNotNull(KafkaSourceFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @Test
        @DisplayName("Should successfully build unbounded source with stopping-offsets and verify CONTINUOUS_UNBOUNDED boundedness")
        void shouldBuildUnboundedSourceWithStoppingOffsets() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.UNBOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                null
            );

            var source = KafkaSourceFactory.supplyFor(config, TEST_SCHEMA);

            assertAll(
                () -> assertNotNull(source),
                () -> assertEquals(Boundedness.CONTINUOUS_UNBOUNDED, source.getBoundedness()),
                () -> assertNotNull(KafkaSourceFactory.supplyBuilderFor(config, TEST_SCHEMA))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParametersAreNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
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
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSourceFactory.supplyFor(config, (KafkaRecordDeserializationSchema<String>) null)
                    );
                    assertEquals("schema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> KafkaSourceFactory.supplyBuilderFor(null, TEST_SCHEMA));
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSourceFactory.supplyBuilderFor(
                            config,
                            (KafkaRecordDeserializationSchema<String>) null
                        )
                    );
                    assertEquals("schema must not be null", ex.getMessage());
                }
            );
        }
    }

    @Nested
    @DisplayName("Client ID prefix")
    class ClientIdPrefix {

        @Test
        @DisplayName("Should configure the Kafka source builder with the client ID prefix")
        void shouldConfigureClientIdPrefix() throws Exception {
            var config = new KafkaSourceProperties(
                "orders-source",
                List.of("localhost:9092"),
                "orders-group",
                "orders-reader",
                List.of("orders"),
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var builder = KafkaSourceFactory.supplyBuilderFor(config, TEST_SCHEMA);
            var field = KafkaSourceBuilder.class.getDeclaredField("clientIdPrefix");
            field.setAccessible(true);

            assertEquals("orders-reader", field.get(builder));
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
                DEFAULT_STARTING_OFFSETS,
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
            var timestampOffsets = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                null,
                "test-.*",
                timestampOffsets,
                null,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully build with valid OFFSETS offset config")
        void shouldBuildWithPartitionOffsets() {
            var partitionOffsets = new KafkaOffsetProperties(
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetProperties("test-topic", 0, 100L)),
                KafkaOffsetResetStrategy.EARLIEST
            );
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                null,
                "test-.*",
                partitionOffsets,
                null,
                null,
                null
            );

            assertNotNull(KafkaSourceFactory.supplyFor(config, TEST_SCHEMA));
        }
    }

    @Nested
    @DisplayName("DeserializationSchema overloads")
    class DeserializationSchemaOverloads {

        private final KafkaSourceProperties config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "test-group",
                List.of("test-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
        );

        @Test
        @DisplayName("Should build KafkaSource and KafkaSourceBuilder from DeserializationSchema")
        void shouldBuildFromDeserializationSchema() {
            var schema = new SimpleStringSchema();
            var source = KafkaSourceFactory.supplyFor(config, schema);
            var builderSource = KafkaSourceFactory.supplyBuilderFor(config, schema).build();

            assertAll(
                    () -> assertNotNull(source),
                    () -> assertEquals(Types.STRING, source.getProducedType()),
                    () -> assertNotNull(builderSource),
                    () -> assertEquals(Types.STRING, builderSource.getProducedType())
            );
        }

        @Test
        @DisplayName("Should apply builder customizer before building KafkaSource")
        void shouldApplyBuilderCustomizer() {
            var source = KafkaSourceFactory.supplyFor(
                    config,
                    new SimpleStringSchema(),
                    builder -> builder.setBounded(OffsetsInitializer.latest())
            );

            assertAll(
                    () -> assertNotNull(source),
                    () -> assertEquals(Boundedness.BOUNDED, source.getBoundedness())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyBuilderFor parameters are null")
        void shouldThrowExceptionWhenSupplyBuilderForParametersAreNull() {
            assertAll(
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyBuilderFor(null, new SimpleStringSchema())
                        );
                        assertEquals("config must not be null", ex.getMessage());
                    },
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyBuilderFor(
                                        config,
                                        (DeserializationSchema<String>) null
                                )
                        );
                        assertEquals("schema must not be null", ex.getMessage());
                    }
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyFor parameters are null")
        void shouldThrowExceptionWhenSupplyForParametersAreNull() {
            assertAll(
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyFor(null, new SimpleStringSchema())
                        );
                        assertEquals("config must not be null", ex.getMessage());
                    },
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyFor(config, (DeserializationSchema<String>) null)
                        );
                        assertEquals("schema must not be null", ex.getMessage());
                    }
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when customized supplyFor parameters are null")
        void shouldThrowExceptionWhenCustomizedSupplyForParametersAreNull() {
            assertAll(
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyFor(null, new SimpleStringSchema(), builder -> { })
                        );
                        assertEquals("config must not be null", ex.getMessage());
                    },
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyFor(
                                        config,
                                        (DeserializationSchema<String>) null,
                                        builder -> { }
                                )
                        );
                        assertEquals("schema must not be null", ex.getMessage());
                    },
                    () -> {
                        var ex = assertThrows(
                                NullPointerException.class,
                                () -> KafkaSourceFactory.supplyFor(config, new SimpleStringSchema(), null)
                        );
                        assertEquals("builderCustomizer must not be null", ex.getMessage());
                    }
            );
        }
    }
}
