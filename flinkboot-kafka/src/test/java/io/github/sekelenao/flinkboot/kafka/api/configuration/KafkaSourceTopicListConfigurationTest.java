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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSourceTopicListConfiguration")
class KafkaSourceTopicListConfigurationTest {

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
                "topics:\n" +
                "  - topic-a\n" +
                "  - topic-b\n" +
                "starting-offsets: EARLIEST\n" +
                "properties:\n" +
                "  security.protocol: PLAINTEXT\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicListConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals(List.of("topic-a", "topic-b"), config.topics()),
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
                "topics:\n" +
                "  - topic-a\n" +
                "starting-offsets: LATEST\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicListConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-group", config.groupId()),
                () -> assertEquals(List.of("topic-a"), config.topics()),
                () -> assertEquals(KafkaOffsetInitializer.LATEST, config.startingOffsets()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize case-insensitive enums")
        void shouldDeserializeCaseInsensitiveEnums() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topics:\n" +
                "  - topic-a\n" +
                "starting-offsets: committed_earliest\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicListConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(KafkaOffsetInitializer.COMMITTED_EARLIEST, config.startingOffsets())
            );
        }

        @Test
        @DisplayName("Should successfully deserialize TIMESTAMP starting-offsets and starting-offsets-timestamp")
        void shouldDeserializeTimestampOffsets() throws Exception {
            var yaml = "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "group-id: my-group\n" +
                "topics:\n" +
                "  - topic-a\n" +
                "starting-offsets: TIMESTAMP\n" +
                "starting-offsets-timestamp: 1689717600000\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicListConfiguration.class);

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
                "topics:\n" +
                "  - topic-a\n" +
                "starting-offsets: OFFSETS\n" +
                "starting-offsets-partition-offsets:\n" +
                "  - topic: \"my-topic\"\n" +
                "    partition: 0\n" +
                "    offset: 12345\n" +
                "  - topic: \"my-topic\"\n" +
                "    partition: 1\n" +
                "    offset: 12346\n";

            var config = mapper.readValue(yaml, KafkaSourceTopicListConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, config.startingOffsets()),
                () -> assertEquals(
                    List.of(
                        new TopicPartitionConfiguration("my-topic", 0, 12345L),
                        new TopicPartitionConfiguration("my-topic", 1, 12346L)
                    ),
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
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers is empty")
        void shouldFailWhenBootstrapServersIsEmpty() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of(),
                "my-group",
                List.of("topic-a"),
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrapServers")))
            );
        }

        @Test
        @DisplayName("Should fail validation when group-id is blank")
        void shouldFailWhenGroupIdIsBlank() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "   ",
                List.of("topic-a"),
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("groupId")))
            );
        }

        @Test
        @DisplayName("Should fail validation when topics is empty")
        void shouldFailWhenTopicsIsEmpty() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "my-group",
                List.of(),
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topics")))
            );
        }

        @Test
        @DisplayName("Should fail validation when starting-offsets is null")
        void shouldFailWhenStartingOffsetsIsNull() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                null,
                null,
                null,
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startingOffsets")))
            );
        }

        @Test
        @DisplayName("Should fail validation when nested partition offset has invalid properties")
        void shouldFailWhenNestedPartitionOffsetIsInvalid() {
            var config = new KafkaSourceTopicListConfiguration(
                List.of("localhost:9092"),
                "my-group",
                List.of("topic-a"),
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(new TopicPartitionConfiguration("   ", -1, -5L)),
                null
            );

            Set<ConstraintViolation<KafkaSourceTopicListConfiguration>> violations = validator.validate(config);
            assertFalse(violations.isEmpty(), "Should fail validation due to invalid nested values");
        }
    }
}
