package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSourceTopicPatternConfiguration")
class KafkaSourceTopicPatternConfigurationTest {

    private static final YAMLMapper mapper = YAMLMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .findAndAddModules()
        .build();

    private static final Validator validator;
    static {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should successfully deserialize from valid YAML with all fields")
        void shouldDeserializeValidYaml() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topic-pattern: \"^my-topic-.*$\"\n" +
                "starting-offsets: EARLIEST\n" +
                "properties:\n" +
                "  security.protocol: PLAINTEXT\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicPatternConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals("^my-topic-.*$", config.topicPattern()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets()),
                () -> assertEquals(Map.of("security.protocol", "PLAINTEXT"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize successfully from YAML without optional properties")
        void shouldDeserializeWithoutOptionalProperties() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topic-pattern: \"^my-topic-.*$\"\n" +
                "starting-offsets: LATEST\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicPatternConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals("^my-topic-.*$", config.topicPattern()),
                () -> assertEquals(KafkaOffsetInitializer.LATEST, config.startingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should successfully deserialize TIMESTAMP starting-offsets and starting-offsets-timestamp")
        void shouldDeserializeTimestampOffsets() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topic-pattern: \"^my-topic-.*$\"\n" +
                "starting-offsets: TIMESTAMP\n" +
                "starting-offsets-timestamp: 1689717600000\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicPatternConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, config.startingOffsets()),
                () -> assertTrue(config.startingOffsetsTimestamp().isPresent()),
                () -> assertEquals(1689717600000L, config.startingOffsetsTimestamp().getAsLong())
            );
        }

        @Test
        @DisplayName("Should successfully deserialize OFFSETS starting-offsets and starting-offsets-partition-offsets")
        void shouldDeserializePartitionOffsets() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topic-pattern: \"^my-topic-.*$\"\n" +
                "starting-offsets: OFFSETS\n" +
                "starting-offsets-partition-offsets:\n" +
                "  - topic: \"my-topic\"\n" +
                "    partition: 0\n" +
                "    offset: 12345\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicPatternConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, config.startingOffsets()),
                () -> assertEquals(
                    List.of(new TopicPartitionOffsetConfiguration("my-topic", 0, 12345L)),
                    config.startingOffsetsPartitionOffsets()
                )
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid properties")
        void shouldPassValidation() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                "^my-topic-.*$",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers is empty")
        void shouldFailWhenBootstrapServersIsEmpty() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of(),
                "my-group",
                "^my-topic-.*$",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrapServers")))
            );
        }

        @Test
        @DisplayName("Should fail validation when group-id is blank")
        void shouldFailWhenGroupIdIsBlank() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "",
                "^my-topic-.*$",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("groupId")))
            );
        }

        @Test
        @DisplayName("Should fail validation when topic-pattern is null")
        void shouldFailWhenTopicPatternIsNull() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                null,
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topicPattern")))
            );
        }

        @Test
        @DisplayName("Should fail validation when topic-pattern is blank")
        void shouldFailWhenTopicPatternIsBlank() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                "   ",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topicPattern")))
            );
        }

        @Test
        @DisplayName("Should fail validation when starting-offsets is null")
        void shouldFailWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                "^my-topic-.*$",
                null,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startingOffsets")))
            );
        }

        @Test
        @DisplayName("Should fail validation when nested partition offset has invalid properties")
        void shouldFailWhenNestedPartitionOffsetIsInvalid() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                "^my-topic-.*$",
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionOffsetConfiguration("   ", -1, -5L)),
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicPatternConfiguration>> violations = validator.validate(config);
            assertFalse(violations.isEmpty(), "Should fail validation due to invalid nested values");
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("Should return expected values from getters when all parameters are present")
        void testGettersWithAllParameters() {
            var pattern = "^my-topic-.*$";
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                pattern,
                KafkaOffsetInitializer.TIMESTAMP,
                12345L,
                List.of(new TopicPartitionOffsetConfiguration("topic-a", 0, 100L)),
                Map.of("key", "val")
            );

            assertAll(
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals(pattern, config.topicPattern()),
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, config.startingOffsets()),
                () -> assertTrue(config.startingOffsetsTimestamp().isPresent()),
                () -> assertEquals(12345L, config.startingOffsetsTimestamp().getAsLong()),
                () -> assertEquals(List.of(new TopicPartitionOffsetConfiguration("topic-a", 0, 100L)), config.startingOffsetsPartitionOffsets()),
                () -> assertEquals(Map.of("key", "val"), config.properties())
            );
        }

        @Test
        @DisplayName("Should return empty structures from getters when optional parameters are absent")
        void testGettersWithAbsentParameters() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                "^my-topic-.*$",
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertTrue(config.startingOffsetsTimestamp().isEmpty()),
                () -> assertEquals(List.of(), config.startingOffsetsPartitionOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Equals and HashCode should work correctly across all branches")
        void testEqualsAndHashCode() {
            var pattern = "^my-topic-.*$";
            var config1 = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                pattern,
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );
            var config2 = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                pattern,
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );
            var configDiffGroup = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "other-group",
                pattern,
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, null),
                () -> assertNotEquals(config1, "string-object"),
                () -> assertNotEquals(config1, configDiffGroup),
                () -> assertNotNull(config1.toString())
            );
        }
    }
}
