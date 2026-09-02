package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSourcePropertiesException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSourceProperties")
class KafkaSourcePropertiesTest {

    private Validator validator;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        mapper = new ObjectMapper(new YAMLFactory());
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should successfully construct with valid topic list arguments")
        void shouldConstructWithTopicList() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a", "topic-b"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of("client.id", "custom-client")
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals(List.of("topic-a", "topic-b"), config.topics()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets()),
                () -> assertTrue(config.startingOffsetsTimestamp().isEmpty()),
                () -> assertTrue(config.startingOffsetsPartitionOffsets().isEmpty()),
                () -> assertEquals(Map.of("client.id", "custom-client"), config.properties())
            );
        }

        @Test
        @DisplayName("Should successfully construct with valid topic pattern arguments")
        void shouldConstructWithTopicPattern() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                "^my-topic-.*$",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertEquals("^my-topic-.*$", config.topicPattern().orElseThrow()),
                () -> assertEquals(KafkaOffsetInitializer.LATEST, config.startingOffsets())
            );
        }

        @Test
        @DisplayName("Should instantiate without exception when startingOffsets is null (deferring to Bean Validation)")
        void shouldInstantiateWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                null,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("topic-a"), config.topics()),
                () -> assertEquals(null, config.startingOffsets())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when both topics and topic-pattern are configured")
        void shouldThrowWhenBothTopicsAndTopicPatternSpecified() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                "^topic-.*$",
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            ));

            assertEquals("Cannot configure both 'topics' and 'topic-pattern'", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when neither topics nor topic-pattern are configured")
        void shouldThrowWhenNeitherTopicsNorTopicPatternSpecified() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            ));

            assertEquals("Either 'topics' or 'topic-pattern' must be specified", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when topics is empty and topic-pattern is null")
        void shouldThrowWhenTopicsIsEmptyAndPatternIsNull() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of(),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            ));

            assertEquals("Either 'topics' or 'topic-pattern' must be specified", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when topics is null and topic-pattern is blank")
        void shouldThrowWhenTopicsIsNullAndPatternIsBlank() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                "   ",
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            ));

            assertEquals("Either 'topics' or 'topic-pattern' must be specified", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when TIMESTAMP is used without timestamp")
        void shouldThrowWhenTimestampIsMissingForTimestampStrategy() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                null,
                null,
                null
            ));

            assertEquals("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when TIMESTAMP is used with partition offsets")
        void shouldThrowWhenPartitionOffsetsSpecifiedForTimestampStrategy() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                1000L,
                List.of(new TopicPartitionOffsetProperties("topic-a", 0, 100L)),
                null
            ));

            assertEquals("starting-offsets-partition-offsets must not be specified when starting-offsets is TIMESTAMP", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when OFFSETS is used without partition offsets")
        void shouldThrowWhenPartitionOffsetsMissingForOffsetsStrategy() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                null,
                null
            ));

            assertEquals("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when OFFSETS is used with timestamp")
        void shouldThrowWhenTimestampSpecifiedForOffsetsStrategy() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                1000L,
                List.of(new TopicPartitionOffsetProperties("topic-a", 0, 100L)),
                null
            ));

            assertEquals("starting-offsets-timestamp must not be specified when starting-offsets is OFFSETS", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when OFFSETS is used with empty partition offsets list")
        void shouldThrowWhenPartitionOffsetsIsEmptyListForOffsetsStrategy() {
            var exception = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(),
                null
            ));

            assertEquals("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS", exception.getMessage());
        }

        @Test
        @DisplayName("Should allow empty partition offsets list when TIMESTAMP strategy is used")
        void shouldAllowEmptyPartitionOffsetsWhenTimestampStrategy() {
            assertDoesNotThrow(() -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                1000L,
                List.of(),
                null
            ));
        }

        @Test
        @DisplayName("Should allow empty partition offsets list when EARLIEST strategy is used")
        void shouldAllowEmptyPartitionOffsetsWhenEarliestStrategy() {
            assertDoesNotThrow(() -> new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                List.of(),
                null
            ));
        }

        @Test
        @DisplayName("Should throw InvalidKafkaSourcePropertiesException when non-TIMESTAMP/OFFSETS strategy has extra parameters")
        void shouldThrowWhenExtraParametersProvidedForSimpleStrategy() {
            assertAll(
                () -> {
                    var ex = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                        "my-source", List.of("localhost:9092"), "my-group", List.of("topic-a"), null,
                        KafkaOffsetInitializer.EARLIEST, 1000L, null, null
                    ));
                    assertEquals("starting-offsets-timestamp must not be specified when starting-offsets is EARLIEST", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(InvalidKafkaSourcePropertiesException.class, () -> new KafkaSourceProperties(
                        "my-source", List.of("localhost:9092"), "my-group", List.of("topic-a"), null,
                        KafkaOffsetInitializer.LATEST, null, List.of(new TopicPartitionOffsetProperties("topic-a", 0, 100L)), null
                    ));
                    assertEquals("starting-offsets-partition-offsets must not be specified when starting-offsets is LATEST", ex.getMessage());
                }
            );
        }

        @Test
        @DisplayName("Should fail validation when name is blank or null")
        void shouldFailWhenNameIsBlankOrNull() {
            var blankConfig = new KafkaSourceProperties(
                "   ",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            var nullConfig = new KafkaSourceProperties(
                null,
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertFalse(validator.validate(blankConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullConfig).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers is empty or null")
        void shouldFailWhenBootstrapServersIsEmptyOrNull() {
            var emptyConfig = new KafkaSourceProperties(
                "my-source",
                List.of(),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            var nullConfig = new KafkaSourceProperties(
                "my-source",
                null,
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertFalse(validator.validate(emptyConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullConfig).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers contains blank or null element")
        void shouldFailWhenBootstrapServersContainsBlankOrNullElement() {
            var blankConfig = new KafkaSourceProperties(
                "my-source",
                List.of("   "),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            var nullConfig = new KafkaSourceProperties(
                "my-source",
                Collections.singletonList(null),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertFalse(validator.validate(blankConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullConfig).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when groupId is blank or null")
        void shouldFailWhenGroupIdIsBlankOrNull() {
            var blankConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "   ",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            var nullConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                null,
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertFalse(validator.validate(blankConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullConfig).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when topics contains blank or null element")
        void shouldFailWhenTopicsContainsBlankOrNullElement() {
            var blankConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("   "),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            var nullConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                Collections.singletonList(null),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertFalse(validator.validate(blankConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullConfig).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when startingOffsets is null")
        void shouldFailWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                null,
                null,
                null,
                null
            );

            assertFalse(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when startingOffsetsTimestamp is negative")
        void shouldFailWhenTimestampIsNegative() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                -1L,
                null,
                null
            );

            assertFalse(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when partition offset property is invalid")
        void shouldFailWhenPartitionOffsetPropertyIsInvalid() {
            var invalidOffset = new TopicPartitionOffsetProperties("   ", -1, -5L);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(invalidOffset),
                null
            );

            assertFalse(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when partition offsets contains a null element")
        void shouldFailWhenPartitionOffsetsContainsNullElement() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                Collections.singletonList(null),
                null
            );

            assertFalse(validator.validate(config).isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when properties contains null key or value")
        void shouldFailWhenPropertiesContainsNullKeyOrValue() {
            var nullKeyMap = new HashMap<String, String>();
            nullKeyMap.put(null, "val");

            var nullValConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Collections.singletonMap("key", null)
            );

            var nullKeyConfig = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                nullKeyMap
            );

            assertAll(
                () -> assertFalse(validator.validate(nullValConfig).isEmpty()),
                () -> assertFalse(validator.validate(nullKeyConfig).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Getters")
    class GettersTests {

        @Test
        @DisplayName("Should return immutable collections and empty optionals when defaulted")
        void shouldReturnSafeDefaults() {
            var config = new KafkaSourceProperties(
                "my-source",
                null,
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertTrue(config.bootstrapServers().isEmpty()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertTrue(config.startingOffsetsTimestamp().isEmpty()),
                () -> assertTrue(config.startingOffsetsPartitionOffsets().isEmpty()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return unmodifiable views of collections")
        void shouldReturnUnmodifiableCollections() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetProperties("topic-a", 0, 10L)),
                Map.of("k", "v")
            );

            assertAll(
                () -> assertThrows(UnsupportedOperationException.class, () -> config.bootstrapServers().add("other:9092")),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.topics().add("topic-b")),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.startingOffsetsPartitionOffsets().add(new TopicPartitionOffsetProperties("t", 1, 0L))),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.properties().put("k2", "v2"))
            );
        }

        @Test
        @DisplayName("Should return present optionals when configured")
        void shouldReturnPresentOptionals() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                "^pattern-.*$",
                KafkaOffsetInitializer.TIMESTAMP,
                123456L,
                null,
                null
            );

            assertAll(
                () -> assertEquals("^pattern-.*$", config.topicPattern().orElseThrow()),
                () -> assertEquals(123456L, config.startingOffsetsTimestamp().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize from YAML with topic list")
        void shouldDeserializeYamlWithTopics() throws Exception {
            var yaml = "name: \"test-kafka-source\"\n" +
                "bootstrap-servers:\n" +
                "  - \"kafka1:9092\"\n" +
                "  - \"kafka2:9092\"\n" +
                "group-id: \"test-group\"\n" +
                "topics:\n" +
                "  - \"events\"\n" +
                "  - \"metrics\"\n" +
                "starting-offsets: \"EARLIEST\"\n" +
                "properties:\n" +
                "  client.id: \"flink-test\"\n";

            var config = mapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals("test-kafka-source", config.name()),
                () -> assertEquals(List.of("kafka1:9092", "kafka2:9092"), config.bootstrapServers()),
                () -> assertEquals("test-group", config.groupId()),
                () -> assertEquals(List.of("events", "metrics"), config.topics()),
                () -> assertTrue(config.topicPattern().isEmpty()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets()),
                () -> assertEquals(Map.of("client.id", "flink-test"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize from YAML with topic pattern")
        void shouldDeserializeYamlWithTopicPattern() throws Exception {
            var yaml = "name: \"test-kafka-source\"\n" +
                "bootstrap-servers:\n" +
                "  - \"kafka1:9092\"\n" +
                "group-id: \"test-group\"\n" +
                "topic-pattern: \"^events-.*$\"\n" +
                "starting-offsets: \"LATEST\"\n";

            var config = mapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals("test-kafka-source", config.name()),
                () -> assertEquals(List.of("kafka1:9092"), config.bootstrapServers()),
                () -> assertEquals("test-group", config.groupId()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertEquals("^events-.*$", config.topicPattern().orElseThrow()),
                () -> assertEquals(KafkaOffsetInitializer.LATEST, config.startingOffsets())
            );
        }

        @Test
        @DisplayName("Should deserialize from YAML with TIMESTAMP strategy")
        void shouldDeserializeYamlWithTimestamp() throws Exception {
            var yaml = "name: \"test-kafka-source\"\n" +
                "bootstrap-servers:\n" +
                "  - \"kafka1:9092\"\n" +
                "group-id: \"test-group\"\n" +
                "topics:\n" +
                "  - \"events\"\n" +
                "starting-offsets: \"TIMESTAMP\"\n" +
                "starting-offsets-timestamp: 1672531199000\n";

            var config = mapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, config.startingOffsets()),
                () -> assertEquals(1672531199000L, config.startingOffsetsTimestamp().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize from YAML with OFFSETS strategy")
        void shouldDeserializeYamlWithPartitionOffsets() throws Exception {
            var yaml = "name: \"test-kafka-source\"\n" +
                "bootstrap-servers:\n" +
                "  - \"kafka1:9092\"\n" +
                "group-id: \"test-group\"\n" +
                "topics:\n" +
                "  - \"events\"\n" +
                "starting-offsets: \"OFFSETS\"\n" +
                "starting-offsets-partition-offsets:\n" +
                "  - topic: \"events\"\n" +
                "    partition: 0\n" +
                "    offset: 12345\n" +
                "  - topic: \"events\"\n" +
                "    partition: 1\n" +
                "    offset: 67890\n";

            var config = mapper.readValue(yaml, KafkaSourceProperties.class);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, config.startingOffsets()),
                () -> assertEquals(2, config.startingOffsetsPartitionOffsets().size()),
                () -> assertEquals("events", config.startingOffsetsPartitionOffsets().get(0).topic()),
                () -> assertEquals(0, config.startingOffsetsPartitionOffsets().get(0).partition()),
                () -> assertEquals(12345L, config.startingOffsetsPartitionOffsets().get(0).offset()),
                () -> assertEquals("events", config.startingOffsetsPartitionOffsets().get(1).topic()),
                () -> assertEquals(1, config.startingOffsetsPartitionOffsets().get(1).partition()),
                () -> assertEquals(67890L, config.startingOffsetsPartitionOffsets().get(1).offset())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should satisfy reflexive and symmetric equality")
        void shouldSatisfyReflexiveAndSymmetricEquality() {
            var config1 = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of("k", "v")
            );

            var config2 = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of("k", "v")
            );

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config2, config1),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, null),
                () -> assertNotEquals(config1, "string")
            );
        }

        @Test
        @DisplayName("Should be unequal when properties differ")
        void shouldBeUnequalWhenFieldsDiffer() {
            var base = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "grp", List.of("top"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffName = new KafkaSourceProperties(
                "diff", List.of("localhost:9092"), "grp", List.of("top"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffServers = new KafkaSourceProperties(
                "src", List.of("remote:9092"), "grp", List.of("top"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffGroup = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "other", List.of("top"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffTopics = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "grp", List.of("other"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffPattern = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "grp", null, "^pattern$",
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v")
            );

            var diffOffsets = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "grp", List.of("top"), null,
                KafkaOffsetInitializer.LATEST, null, null, Map.of("k", "v")
            );

            var diffProps = new KafkaSourceProperties(
                "src", List.of("localhost:9092"), "grp", List.of("top"), null,
                KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k2", "v2")
            );

            assertAll(
                () -> assertNotEquals(base, diffName),
                () -> assertNotEquals(base, diffServers),
                () -> assertNotEquals(base, diffGroup),
                () -> assertNotEquals(base, diffTopics),
                () -> assertNotEquals(base, diffPattern),
                () -> assertNotEquals(base, diffOffsets),
                () -> assertNotEquals(base, diffProps)
            );
        }

        @Test
        @DisplayName("Should produce expected toString representation")
        void shouldProduceToString() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of("k", "v")
            );

            var str = config.toString();
            assertAll(
                () -> assertTrue(str.contains("KafkaSourceProperties")),
                () -> assertTrue(str.contains("my-source")),
                () -> assertTrue(str.contains("localhost:9092")),
                () -> assertTrue(str.contains("my-group")),
                () -> assertTrue(str.contains("topic-a")),
                () -> assertTrue(str.contains("EARLIEST"))
            );
        }
    }
}
