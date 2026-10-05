package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.sekelenao.flinkboot.core.internal.validation.Validators;

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
        new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null);

    private static final KafkaOffsetProperties DEFAULT_STOPPING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null);

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
                List.of("topic-a", "topic-b"),
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
                "^my-topic-.*$",
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals("my-source", config.name()),
                () -> assertTrue(config.topics().isEmpty()),
                () -> assertEquals("^my-topic-.*$", config.topicPattern().orElseThrow()),
                () -> assertEquals(Optional.empty(), config.boundedness()),
                () -> assertEquals(Optional.empty(), config.stoppingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty list when bootstrapServers is null")
        void shouldReturnEmptyListWhenBootstrapServersIsNull() {
            var config = new KafkaSourceProperties(
                "my-source",
                null,
                "my-group",
                List.of("topic-a"),
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                null
            );

            assertTrue(config.bootstrapServers().isEmpty());
        }

        @Test
        @DisplayName("Should return unmodifiable defensive copies for collections")
        void shouldReturnUnmodifiableCollections() {
            var servers = new ArrayList<>(List.of("localhost:9092"));
            var topics = new ArrayList<>(List.of("topic-a"));
            var properties = new HashMap<>(Map.of("k", "v"));

            var config = new KafkaSourceProperties(
                "my-source",
                servers,
                "my-group",
                topics,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                properties
            );

            assertAll(
                () -> assertThrows(UnsupportedOperationException.class, () -> config.bootstrapServers().add("other:9092")),
                () -> assertThrows(UnsupportedOperationException.class, () -> config.topics().add("topic-b")),
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
                List.of("topic-a"),
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
                List.of("topic-a"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
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
                List.of("topic-a"),
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
                List.of("topic-a"),
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
                List.of("topic-a"),
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
                List.of("topic-a"),
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
        @DisplayName("Should fail validation when topicPattern is blank")
        void shouldFailWhenTopicPatternIsBlank(String blankPattern) {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                null,
                blankPattern,
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
                List.of("topic-a"),
                "^topic-.*$",
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
                    && v.getMessage().contains("Cannot configure both 'topics' and 'topic-pattern'")))
            );
        }

        @Test
        @DisplayName("Should fail validation when neither topics nor topic-pattern are configured")
        void shouldFailWhenNeitherTopicsNorTopicPatternSpecified() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
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
                    && v.getMessage().contains("Either 'topics' or 'topic-pattern' must be specified")))
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

            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("starting-offsets")))
            );
        }

        @Test
        @DisplayName("Should cascade validation into startingOffsets when invalid")
        void shouldCascadeValidationIntoStartingOffsets() {
            var invalidOffsets = new KafkaOffsetProperties(null, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
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
                List.of("topic-a"),
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
        @DisplayName("Should cascade validation into stoppingOffsets when invalid")
        void shouldCascadeValidationIntoStoppingOffsets() {
            var invalidOffsets = new KafkaOffsetProperties(null, null, null);
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
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
                () -> assertEquals(List.of("orders", "payments"), config.topics()),
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
                () -> assertEquals(List.of("orders"), config.topics()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets().strategy()),
                () -> assertEquals(Optional.empty(), config.boundedness()),
                () -> assertEquals(Optional.empty(), config.stoppingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
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
                List.of("topic"),
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
                List.of("topic"),
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
                List.of("topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of("k", "v")
            );

            var diffName = new KafkaSourceProperties("other", List.of("localhost:9092"), "group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffServers = new KafkaSourceProperties("source", List.of("localhost:9093"), "group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffGroup = new KafkaSourceProperties("source", List.of("localhost:9092"), "other-group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffTopics = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("other-topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffPattern = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("topic"), "^topic.*", DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffStart = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("topic"), null, new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null), KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffBoundedness = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.UNBOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v"));
            var diffStop = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null), Map.of("k", "v"));
            var diffProps = new KafkaSourceProperties("source", List.of("localhost:9092"), "group", List.of("topic"), null, DEFAULT_STARTING_OFFSETS, KafkaBoundedness.BOUNDED, DEFAULT_STOPPING_OFFSETS, Map.of("k", "v2"));

            assertAll(
                () -> assertNotEquals(base, diffName),
                () -> assertNotEquals(base, diffServers),
                () -> assertNotEquals(base, diffGroup),
                () -> assertNotEquals(base, diffTopics),
                () -> assertNotEquals(base, diffPattern),
                () -> assertNotEquals(base, diffStart),
                () -> assertNotEquals(base, diffBoundedness),
                () -> assertNotEquals(base, diffStop),
                () -> assertNotEquals(base, diffProps),
                () -> assertNotEquals(base, null),
                () -> assertNotEquals(base, "other type")
            );
        }

        @Test
        @DisplayName("Should produce meaningful toString output")
        void shouldProduceMeaningfulToString() {
            var config = new KafkaSourceProperties(
                "my-source",
                List.of("localhost:9092"),
                "my-group",
                List.of("topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            var str = config.toString();
            assertAll(
                () -> assertTrue(str.contains("my-source")),
                () -> assertTrue(str.contains("KafkaSourceProperties"))
            );
        }
    }
}
