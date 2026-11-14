package com.flinkboot.kafka.api.sink;

import com.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchemaBuilder;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSinkFactory")
class KafkaSinkFactoryTest {

    private static final KafkaRecordSerializationSchema<String> TEST_SCHEMA = (element, context, timestamp) ->
        new ProducerRecord<>("my-topic", element.getBytes());

    private static final KafkaSinkProperties DEFAULT_CONFIG = new KafkaSinkProperties(
        "my-sink",
        List.of("localhost:9092"),
        "my-topic",
        KafkaDeliveryGuarantee.AT_LEAST_ONCE,
        null,
        Map.of("client.id", "test-client")
    );

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = KafkaSinkFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (KafkaRecordSerializationSchema)")
    class SupplyRecordSerializationSchema {

        @Test
        @DisplayName("Should successfully build KafkaSink and KafkaSinkBuilder with default configuration")
        void shouldBuildKafkaSink() {
            assertAll(
                () -> assertNotNull(KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, TEST_SCHEMA)),
                () -> assertNotNull(KafkaSinkFactory.supplyBuilderFor(DEFAULT_CONFIG, TEST_SCHEMA))
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
        @DisplayName("Should apply builder customizer before building KafkaSink")
        void shouldApplyBuilderCustomizer() {
            var customized = new AtomicBoolean();
            var sink = KafkaSinkFactory.supplyFor(
                DEFAULT_CONFIG,
                TEST_SCHEMA,
                builder -> {
                    assertNotNull(builder);
                    customized.set(true);
                }
            );

            assertAll(
                () -> assertNotNull(sink),
                () -> assertTrue(customized.get())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor((KafkaSinkProperties) null, TEST_SCHEMA)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, (KafkaRecordSerializationSchema<String>) null)
                    );
                    assertEquals("serializationSchema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor((KafkaSinkProperties) null, TEST_SCHEMA)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor(DEFAULT_CONFIG, (KafkaRecordSerializationSchema<String>) null)
                    );
                    assertEquals("serializationSchema must not be null", ex.getMessage());
                }
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when customized supplyFor parameters are null")
        void shouldThrowExceptionWhenCustomizedSupplyForParamsAreNull() {
            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(null, TEST_SCHEMA, builder -> {})
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            (KafkaRecordSerializationSchema<String>) null,
                            builder -> {}
                        )
                    );
                    assertEquals("serializationSchema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, TEST_SCHEMA, null)
                    );
                    assertEquals("builderCustomizer must not be null", ex.getMessage());
                }
            );
        }
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (Consumer<KafkaRecordSerializationSchemaBuilder>)")
    class SupplySchemaCustomizer {

        @Test
        @DisplayName("Should successfully build KafkaSink and KafkaSinkBuilder with schema customizer")
        void shouldBuildWithSchemaCustomizer() throws Exception {
            Consumer<KafkaRecordSerializationSchemaBuilder<String>> customizer = builder ->
                builder.setValueSerializationSchema(new SimpleStringSchema())
                    .setKeySerializationSchema(element -> ("k-" + element).getBytes(StandardCharsets.UTF_8));

            var sink = KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, customizer);
            var builderSink = KafkaSinkFactory.supplyBuilderFor(DEFAULT_CONFIG, customizer).build();

            var field = KafkaSink.class.getDeclaredField("recordSerializer");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            var sinkSerializer = (KafkaRecordSerializationSchema<String>) field.get(sink);
            @SuppressWarnings("unchecked")
            var builderSerializer = (KafkaRecordSerializationSchema<String>) field.get(builderSink);

            var sinkRecord = sinkSerializer.serialize("payload", null, null);
            var builderRecord = builderSerializer.serialize("payload", null, null);

            assertAll(
                () -> assertNotNull(sink),
                () -> assertNotNull(builderSink),
                () -> assertEquals("my-topic", sinkRecord.topic()),
                () -> assertEquals("k-payload", new String(sinkRecord.key(), StandardCharsets.UTF_8)),
                () -> assertEquals("payload", new String(sinkRecord.value(), StandardCharsets.UTF_8)),
                () -> assertEquals("my-topic", builderRecord.topic()),
                () -> assertEquals("k-payload", new String(builderRecord.key(), StandardCharsets.UTF_8)),
                () -> assertEquals("payload", new String(builderRecord.value(), StandardCharsets.UTF_8))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyBuilderFor parameters are null")
        void shouldThrowExceptionWhenSupplyBuilderForParamsAreNull() {
            Consumer<KafkaRecordSerializationSchemaBuilder<String>> customizer = builder ->
                builder.setValueSerializationSchema(new SimpleStringSchema());

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor((KafkaSinkProperties) null, customizer)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor(
                            DEFAULT_CONFIG,
                            (Consumer<KafkaRecordSerializationSchemaBuilder<String>>) null
                        )
                    );
                    assertEquals("schemaCustomizer must not be null", ex.getMessage());
                }
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyFor parameters are null")
        void shouldThrowExceptionWhenSupplyForParamsAreNull() {
            Consumer<KafkaRecordSerializationSchemaBuilder<String>> customizer = builder ->
                builder.setValueSerializationSchema(new SimpleStringSchema());

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor((KafkaSinkProperties) null, customizer)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            (Consumer<KafkaRecordSerializationSchemaBuilder<String>>) null
                        )
                    );
                    assertEquals("schemaCustomizer must not be null", ex.getMessage());
                }
            );
        }
    }

    @Nested
    @DisplayName("supplyFor (Consumer<KafkaRecordSerializationSchemaBuilder> and Consumer<KafkaSinkBuilder>)")
    class SupplySchemaAndBuilderCustomizers {

        @Test
        @DisplayName("Should apply schema customizer and builder customizer before building KafkaSink")
        void shouldApplySchemaAndBuilderCustomizers() throws Exception {
            Consumer<KafkaRecordSerializationSchemaBuilder<String>> schemaCustomizer = builder ->
                builder.setValueSerializationSchema(new SimpleStringSchema());
            var builderCustomized = new AtomicBoolean();
            Consumer<KafkaSinkBuilder<String>> builderCustomizer = builder -> {
                assertNotNull(builder);
                builderCustomized.set(true);
            };

            var sink = KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, schemaCustomizer, builderCustomizer);

            var field = KafkaSink.class.getDeclaredField("recordSerializer");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            var sinkSerializer = (KafkaRecordSerializationSchema<String>) field.get(sink);
            var sinkRecord = sinkSerializer.serialize("payload", null, null);

            assertAll(
                () -> assertNotNull(sink),
                () -> assertTrue(builderCustomized.get()),
                () -> assertEquals("my-topic", sinkRecord.topic()),
                () -> assertEquals("payload", new String(sinkRecord.value(), StandardCharsets.UTF_8))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            Consumer<KafkaRecordSerializationSchemaBuilder<String>> schemaCustomizer = builder -> {};
            Consumer<KafkaSinkBuilder<String>> builderCustomizer = builder -> {};

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor((KafkaSinkProperties) null, schemaCustomizer, builderCustomizer)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            (Consumer<KafkaRecordSerializationSchemaBuilder<String>>) null,
                            builderCustomizer
                        )
                    );
                    assertEquals("schemaCustomizer must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, schemaCustomizer, null)
                    );
                    assertEquals("builderCustomizer must not be null", ex.getMessage());
                }
            );
        }
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor (SerializationSchema)")
    class SupplySerializationSchema {

        @Test
        @DisplayName("Should successfully build KafkaSink and KafkaSinkBuilder with SerializationSchema targeting config topic")
        void shouldBuildWithSerializationSchema() throws Exception {
            var schema = new SimpleStringSchema();
            var sink = KafkaSinkFactory.supplyFor(DEFAULT_CONFIG, schema);
            var builderSink = KafkaSinkFactory.supplyBuilderFor(DEFAULT_CONFIG, schema).build();

            var field = KafkaSink.class.getDeclaredField("recordSerializer");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            var sinkSerializer = (KafkaRecordSerializationSchema<String>) field.get(sink);
            @SuppressWarnings("unchecked")
            var builderSerializer = (KafkaRecordSerializationSchema<String>) field.get(builderSink);

            var sinkRecord = sinkSerializer.serialize("payload", null, null);
            var builderRecord = builderSerializer.serialize("payload", null, null);

            assertAll(
                () -> assertEquals("my-topic", sinkRecord.topic()),
                () -> assertNull(sinkRecord.key()),
                () -> assertEquals("payload", new String(sinkRecord.value(), StandardCharsets.UTF_8)),
                () -> assertEquals("my-topic", builderRecord.topic()),
                () -> assertNull(builderRecord.key()),
                () -> assertEquals("payload", new String(builderRecord.value(), StandardCharsets.UTF_8))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyBuilderFor parameters are null")
        void shouldThrowExceptionWhenSupplyBuilderForParamsAreNull() {
            var schema = new SimpleStringSchema();

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor((KafkaSinkProperties) null, schema)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyBuilderFor(
                            DEFAULT_CONFIG,
                            (SerializationSchema<String>) null
                        )
                    );
                    assertEquals("valueSerializationSchema must not be null", ex.getMessage());
                }
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when supplyFor parameters are null")
        void shouldThrowExceptionWhenSupplyForParamsAreNull() {
            var schema = new SimpleStringSchema();

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor((KafkaSinkProperties) null, schema)
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            (SerializationSchema<String>) null
                        )
                    );
                    assertEquals("valueSerializationSchema must not be null", ex.getMessage());
                }
            );
        }

        @Test
        @DisplayName("Should apply builder customizer before building KafkaSink")
        void shouldApplyBuilderCustomizer() {
            var customized = new AtomicBoolean();
            var schema = new SimpleStringSchema();
            var sink = KafkaSinkFactory.supplyFor(
                DEFAULT_CONFIG,
                schema,
                builder -> {
                    assertNotNull(builder);
                    customized.set(true);
                }
            );

            assertAll(
                () -> assertNotNull(sink),
                () -> assertTrue(customized.get())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when customized supplyFor parameters are null")
        void shouldThrowExceptionWhenCustomizedSupplyForParamsAreNull() {
            var schema = new SimpleStringSchema();

            assertAll(
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            (KafkaSinkProperties) null,
                            schema,
                            builder -> {}
                        )
                    );
                    assertEquals("config must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            (SerializationSchema<String>) null,
                            builder -> {}
                        )
                    );
                    assertEquals("valueSerializationSchema must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(
                        NullPointerException.class,
                        () -> KafkaSinkFactory.supplyFor(
                            DEFAULT_CONFIG,
                            schema,
                            null
                        )
                    );
                    assertEquals("builderCustomizer must not be null", ex.getMessage());
                }
            );
        }
    }
}
