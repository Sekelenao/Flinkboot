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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                () -> assertEquals("^my-topic-.*$", config.topicPattern().pattern()),
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, config.startingOffsets()),
                () -> assertTrue(config.properties().isPresent()),
                () -> assertEquals(Map.of("security.protocol", "PLAINTEXT"), config.properties().get())
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
                () -> assertEquals("^my-topic-.*$", config.topicPattern().pattern()),
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
                () -> assertEquals(1689717600000L, config.startingOffsetsTimestamp().get())
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
                "  my-topic-0: 12345\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicPatternConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, config.startingOffsets()),
                () -> assertTrue(config.startingOffsetsPartitionOffsets().isPresent()),
                () -> assertEquals(Map.of("my-topic-0", 12345L), config.startingOffsetsPartitionOffsets().get())
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
                Pattern.compile("^my-topic-.*$"),
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
                Pattern.compile("^my-topic-.*$"),
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
                Pattern.compile("^my-topic-.*$"),
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
        @DisplayName("Should fail validation when starting-offsets is null")
        void shouldFailWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceTopicPatternConfiguration(
                List.of("localhost:9092"),
                "my-group",
                Pattern.compile("^my-topic-.*$"),
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
    }
}
