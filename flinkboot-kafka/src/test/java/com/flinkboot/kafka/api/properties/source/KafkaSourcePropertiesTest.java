package com.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flinkboot.core.internal.validation.Validators;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSourceProperties")
class KafkaSourcePropertiesTest {

    private static final Validator validator;

    static {
        try (var factory = Validators.factory()) {
            validator = factory.getValidator();
        }
    }

    private static final KafkaOffsetProperties DEFAULT_STARTING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null);

    private static final KafkaOffsetProperties DEFAULT_STOPPING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Should successfully construct with topic list arguments")
        void shouldConstructWithTopicList() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a", "topic-b"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of("client.id", "custom-client")
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals(List.of("topic-a", "topic-b"), config.topics()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertTrue(config.partitions().isEmpty()),
                () -> assertEquals(DEFAULT_STARTING_OFFSETS, config.startingOffsets()),
                () -> assertEquals(Optional.of(KafkaBoundedness.BOUNDED), config.boundedness()),
                () -> assertEquals(Optional.of(DEFAULT_STOPPING_OFFSETS), config.stoppingOffsets()),
                () -> assertEquals(Map.of("client.id", "custom-client"), config.properties())
            );
        }

        @Test
        @DisplayName("Should successfully construct with topic pattern arguments")
        void shouldConstructWithTopicPattern() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                "^my-topic-.*$",
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertEquals("^my-topic-.*$", config.topicPattern().orElseThrow()),
                () -> assertTrue(config.partitions().isEmpty()),
                () -> assertEquals(Optional.empty(), config.boundedness()),
                () -> assertEquals(Optional.empty(), config.stoppingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should successfully construct with partition list arguments")
        void shouldConstructWithPartitionList() {
            var partitions = List.of(new TopicPartitionProperties("topic-a", 0), new TopicPartitionProperties("topic-a", 1));
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                null,
                partitions,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertEquals(partitions, config.partitions())
            );
        }

        @Test
        @DisplayName("Should return an empty or configured client ID prefix from the canonical constructor")
        void shouldConstructWithOptionalClientIdPrefix() {
            var absent = new KafkaSourceProperties("my-source", List.of("localhost:9092"), "my-group", null,
                List.of("topic-a"), null, null, DEFAULT_STARTING_OFFSETS, null, null, null);
            var configured = new KafkaSourceProperties("my-source", List.of("localhost:9092"), "my-group", "orders-reader",
                List.of("topic-a"), null, null, DEFAULT_STARTING_OFFSETS, null, null, null);
            assertAll(
                () -> assertEquals(Optional.empty(), absent.clientIdPrefix()),
                () -> assertEquals(Optional.of("orders-reader"), configured.clientIdPrefix())
            );
        }

        @Test
        @DisplayName("Should return empty list when bootstrapServers is null")
        void shouldReturnEmptyListWhenBootstrapServersIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                null,
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(config.bootstrapServers().isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when partitions is null")
        void shouldReturnEmptyListWhenPartitionsIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(config.partitions().isEmpty());
        }

        @Test
        @DisplayName("Should return unmodifiable defensive copies for collections")
        void shouldReturnUnmodifiableCollections() {
            var servers = new ArrayList<>(List.of("localhost:9092"));
            var topics = new ArrayList<>(List.of("topic-a"));
            var partitions = new ArrayList<>(List.of(new TopicPartitionProperties("topic-a", 0)));
            var properties = new HashMap<>(Map.of("k", "v"));

            var config = new KafkaSourceProperties(
                "my-source",
                servers,
                "my-group",
                null,
                topics,
                null,
                partitions,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                properties
            );

            assertAll(
                () -> assertThrows(UnsupportedOperationException.class, () -> config.bootstrapServers().add("other:9092")),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.topics().add("topic-b")),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.partitions().add(new TopicPartitionProperties("topic-b", 1))),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.properties().put("k2", "v2"))
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation with valid unbounded properties")
        void shouldPassWithValidUnboundedProperties() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with valid bounded properties")
        void shouldPassWithValidBoundedProperties() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                null
            );

            assertTrue(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with valid partitions subscription")
        void shouldPassWithValidPartitions() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                null,
                List.of(new TopicPartitionProperties("topic-a", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with valid client ID prefix")
        void shouldPassWithValidClientIdPrefix() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                "orders-reader",
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(validator.validate(config).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should fail validation when name is null, empty, or blank")
        void shouldFailWhenNameIsInvalid(String name) {
            var config = new KafkaSourceProperties(
                name,
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")))
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrapServers is null or empty")
        void shouldFailWhenBootstrapServersIsNullOrEmpty() {
            var configNull = new KafkaSourceProperties(
                "my-source",
                null,
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );
            var configEmpty = new KafkaSourceProperties(
                "my-source",
                List.of(),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertAll(
                () -> assertTrue(validator.validate(configNull).stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrap-servers"))),
                () -> assertTrue(validator.validate(configEmpty).stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrap-servers")))
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should fail validation when groupId is null, empty, or blank")
        void shouldFailWhenGroupIdIsInvalid(String groupId) {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                groupId,
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("group-id")))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail validation when clientIdPrefix is blank")
        void shouldFailWhenClientIdPrefixIsBlank(String clientIdPrefix) {
            var config = new KafkaSourceProperties("my-source", List.of("localhost:9092"), "my-group", clientIdPrefix,
                List.of("topic-a"), null, null, DEFAULT_STARTING_OFFSETS, null, null, null);
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("client-id-prefix")))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail validation when topicPattern is blank")
        void shouldFailWhenTopicPatternIsBlank(String blankPattern) {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                blankPattern,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );
            var violations = validator.validate(config);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topic-pattern")))
            );
        }

        @Test
        @DisplayName("Should fail validation when both topics and topic-pattern are configured")
        void shouldFailWhenBothTopicsAndTopicPatternSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                "^topic-.*$",
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("topic-pattern")
                    && v.getMessage().contains("Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified")))
            );
        }

        @Test
        @DisplayName("Should fail validation when both topics and partitions are configured")
        void shouldFailWhenBothTopicsAndPartitionsSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                List.of(new TopicPartitionProperties("topic-a", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified")))
            );
        }

        @Test
        @DisplayName("Should fail validation when both topic-pattern and partitions are configured")
        void shouldFailWhenBothTopicPatternAndPartitionsSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                "^topic-.*$",
                List.of(new TopicPartitionProperties("topic-a", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified")))
            );
        }

        @Test
        @DisplayName("Should fail validation when all three topics, topic-pattern, and partitions are configured")
        void shouldFailWhenAllThreeSubscriptionModesSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                "^topic-.*$",
                List.of(new TopicPartitionProperties("topic-a", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified")))
            );
        }

        @Test
        @DisplayName("Should fail validation when neither topics, topic-pattern, nor partitions are configured")
        void shouldFailWhenNeitherTopicsNorTopicPatternSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("topics")
                    && v.getMessage().contains("Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified")))
            );
        }

        @Test
        @DisplayName("Should fail validation when partitions list contains null or invalid elements")
        void shouldFailWhenPartitionsContainInvalidElement() {
            var configWithNull = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                null,
                Collections.singletonList(null),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(configWithNull);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("partitions")))
            );
        }

        @Test
        @DisplayName("Should cascade validation into partitions when element is invalid")
        void shouldCascadeValidationIntoPartitions() {
            var invalidPartition = new TopicPartitionProperties(null, -1);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                null,
                List.of(invalidPartition),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(2, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partitions[0].topic"))),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partitions[0].partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when startingOffsets is null")
        void shouldFailWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                null,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("starting-offsets")))
            );
        }

        @Test
        @DisplayName("Should cascade validation into startingOffsets when invalid")
        void shouldCascadeValidationIntoStartingOffsets() {
            var invalidOffsets = new KafkaOffsetProperties(null, null, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                invalidOffsets,
                null,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("starting-offsets")))
            );
        }

        @Test
        @DisplayName("Should fail validation when boundedness is BOUNDED and stoppingOffsets is missing")
        void shouldFailWhenBoundedWithoutStoppingOffsets() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("stopping-offsets")
                    && v.getMessage().contains("stopping-offsets is required when boundedness is BOUNDED")))
            );
        }

        @Test
        @DisplayName("Should fail validation when stoppingOffsets is configured but boundedness is missing")
        void shouldFailWhenStoppingOffsetsConfiguredWithoutBoundedness() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                DEFAULT_STOPPING_OFFSETS,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("boundedness")
                    && v.getMessage().contains("boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured")))
            );
        }

        @Test
        @DisplayName("Should cascade validation into stoppingOffsets when invalid")
        void shouldCascadeValidationIntoStoppingOffsets() {
            var invalidOffsets = new KafkaOffsetProperties(null, null, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                List.of("topic-a"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                invalidOffsets,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("stopping-offsets")))
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should deserialize full YAML with nested starting and stopping offsets")
        void shouldDeserializeFullYaml() throws Exception {
            var yaml = "name: \"full-kafka-source\"\n"
                + "bootstrap-servers:\n"
                + "  - \"localhost:9092\"\n"
                + "  - \"localhost:9093\"\n"
                + "group-id: \"analytics-group\"\n"
                + "client-id-prefix: \"analytics-reader\"\n"
                + "topics:\n"
                + "  - \"orders\"\n"
                + "  - \"payments\"\n"
                + "boundedness: BOUNDED\n"
                + "starting-offsets:\n"
                + "  strategy: TIMESTAMP\n"
                + "  timestamp: 1689717600000\n"
                + "stopping-offsets:\n"
                + "  strategy: TIMESTAMP\n"
                + "  timestamp: 1689721200000\n"
                + "properties:\n"
                + "  client.id: \"analytics-worker\"\n";

            var config = yamlMapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals("full-kafka-source", config.name()),
                () -> assertEquals(List.of("localhost:9092", "localhost:9093"), config.bootstrapServers()),
                () -> assertEquals("analytics-group", config.groupId()),
                () -> assertEquals(Optional.of("analytics-reader"), config.clientIdPrefix()),
                () -> assertEquals(List.of("orders", "payments"), config.topics()),
                () -> assertTrue(config.partitions().isEmpty()),
                () -> assertEquals(Optional.of(KafkaBoundedness.BOUNDED), config.boundedness()),
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, config.startingOffsets().strategy()),
                () -> assertEquals(1689717600000L, config.startingOffsets().timestamp().orElseThrow()),
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, config.stoppingOffsets().orElseThrow().strategy()),
                () -> assertEquals(1689721200000L, config.stoppingOffsets().orElseThrow().timestamp().orElseThrow()),
                () -> assertEquals(Map.of("client.id", "analytics-worker"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize minimal YAML with default optional fields")
        void shouldDeserializeMinimalYaml() throws Exception {
            var yaml = "name: \"minimal-kafka-source\"\n"
                + "bootstrap-servers:\n"
                + "  - \"localhost:9092\"\n"
                + "group-id: \"minimal-group\"\n"
                + "topics:\n"
                + "  - \"orders\"\n"
                + "starting-offsets:\n"
                + "  strategy: EARLIEST\n";

            var config = yamlMapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals("minimal-kafka-source", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("minimal-group", config.groupId()),
                () -> assertTrue(config.clientIdPrefix().isEmpty()),
                () -> assertEquals(List.of("orders"), config.topics()),
                () -> assertTrue(config.partitions().isEmpty()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets().strategy()),
                () -> assertEquals(Optional.empty(), config.boundedness()),
                () -> assertEquals(Optional.empty(), config.stoppingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize YAML with partition subscriptions")
        void shouldDeserializeYamlWithPartitions() throws Exception {
            var yaml = "name: \"partition-kafka-source\"\n"
                + "bootstrap-servers:\n"
                + "  - \"localhost:9092\"\n"
                + "group-id: \"partition-group\"\n"
                + "partitions:\n"
                + "  - topic: \"orders\"\n"
                + "    partition: 0\n"
                + "  - topic: \"orders\"\n"
                + "    partition: 1\n"
                + "starting-offsets:\n"
                + "  strategy: EARLIEST\n";

            var config = yamlMapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals("partition-kafka-source", config.name()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertEquals(2, config.partitions().size()),
                () -> assertEquals("orders", config.partitions().get(0).topic()),
                () -> assertEquals(0, config.partitions().get(0).partition()),
                () -> assertEquals("orders", config.partitions().get(1).topic()),
                () -> assertEquals(1, config.partitions().get(1).partition()),
                () -> assertTrue(validator.validate(config).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should verify reflexive, symmetric equality and hashCode")
        void shouldVerifyEquality() {
            var a = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of("k", "v")
            );
            var b = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of("k", "v")
            );

            assertAll(
                () -> assertEquals(a, a),
                () -> assertEquals(a, b),
                () -> assertEquals(b, a),
                () -> assertEquals(a.hashCode(), b.hashCode())
            );
        }

        @Test
        @DisplayName("Should verify inequality when fields differ")
        void shouldVerifyInequality() {
            var base = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of("k", "v")
            );

            var diffName = new KafkaSourceProperties("other", List.of("localhost:9092"), "group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffServers = new KafkaSourceProperties("source", List.of("localhost:9093"), "group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffGroup = new KafkaSourceProperties("source", List.of("localhost:9092"), "other-group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffTopics = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, List.of("other-topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffPattern = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, null, "^topic.*", null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffPartitions = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, null, null, List.of(new TopicPartitionProperties("topic", 0)), DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffStart = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, List.of("topic"), null, null, new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null), KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffBoundedness = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.UNBOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffStop = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null, null), Map.of("k", "v"));
            var diffProps = new KafkaSourceProperties("source", List.of("localhost:9092"), "group",
                null, List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v2"));

            assertAll(
                () -> assertNotEquals(base, diffName),
                () -> assertNotEquals(base, diffServers),
                () -> assertNotEquals(base, diffGroup),
                () -> assertNotEquals(base, diffTopics),
                () -> assertNotEquals(base, diffPattern),
                () -> assertNotEquals(base, diffPartitions),
                () -> assertNotEquals(base, diffStart),
                () -> assertNotEquals(base, diffBoundedness),
                () -> assertNotEquals(base, diffStop),
                () -> assertNotEquals(base, diffProps),
                () -> assertNotEquals(base, null),
                () -> assertNotEquals(base, "other type")
            );
        }

        @Test
        @DisplayName("Should distinguish configurations with different client ID prefixes")
        void shouldDistinguishClientIdPrefixes() {
            var base = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                "orders-reader",
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );
            var differentPrefix = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                "payments-reader",
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertAll(
                () -> assertNotEquals(base, differentPrefix),
                () -> assertNotEquals(differentPrefix, base),
                () -> assertNotEquals(base.hashCode(), differentPrefix.hashCode())
            );
        }

        @Test
        @DisplayName("Should distinguish configurations with and without a client ID prefix")
        void shouldCompareNullAndNonNullClientIdPrefixes() {
            var absent = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", null,
                List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, null, null, null);
            var configured = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", "orders-reader",
                List.of("topic"), null, null, DEFAULT_STARTING_OFFSETS, null, null, null);
            assertAll(
                () -> assertNotEquals(absent, configured),
                () -> assertNotEquals(configured, absent),
                () -> assertNotEquals(absent.hashCode(), configured.hashCode())
            );
        }

        @Test
        @DisplayName("Should produce meaningful toString output")
        void shouldProduceMeaningfulToString() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                "orders-reader",
                List.of("topic"),
                null,
                List.of(new TopicPartitionProperties("topic", 0)),
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            var str = config.toString();
            assertAll(
                () -> assertTrue(str.contains("my-source")),
                () -> assertTrue(str.contains("clientIdPrefix='orders-reader'")),
                () -> assertTrue(str.contains("partitions=")),
                () -> assertTrue(str.contains("KafkaSourceProperties"))
            );
        }
    }
}
