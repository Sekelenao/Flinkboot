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

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TopicPartitionOffsetProperties")
class TopicPartitionOffsetPropertiesTest {

    private static final Validator validator;

    static {
        try (var factory = buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Should return expected property values from getters")
        void shouldReturnExpectedValues() {
            var config = new TopicPartitionOffsetProperties("topic-a", 2, 500L);

            assertAll(
                () -> assertEquals("topic-a", config.topic()),
                () -> assertEquals(2, config.partition()),
                () -> assertEquals(500L, config.offset())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation with valid properties")
        void shouldPassValidationWithValidProperties() {
            var config = new TopicPartitionOffsetProperties("topic-a", 0, 100L);
            var violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Expected no validation violations");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should fail validation when topic is null, empty, or blank")
        void shouldFailWhenTopicIsNullOrEmptyOrBlank(String topic) {
            var config = new TopicPartitionOffsetProperties(topic, 0, 100L);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topic")))
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is null")
        void shouldFailWhenPartitionIsNull() {
            var config = new TopicPartitionOffsetProperties("topic-a", null, 100L);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when offset is null")
        void shouldFailWhenOffsetIsNull() {
            var config = new TopicPartitionOffsetProperties("topic-a", 0, null);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("offset")))
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is negative")
        void shouldFailWhenPartitionIsNegative() {
            var config = new TopicPartitionOffsetProperties("topic-a", -1, 100L);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when offset is negative")
        void shouldFailWhenOffsetIsNegative() {
            var config = new TopicPartitionOffsetProperties("topic-a", 0, -100L);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("offset")))
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should successfully deserialize from valid YAML")
        void shouldDeserializeFromValidYaml() throws Exception {
            var yaml = "topic: \"events\"\n"
                + "partition: 3\n"
                + "offset: 450\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionOffsetProperties.class);

            assertAll(
                () -> assertEquals("events", config.topic()),
                () -> assertEquals(3, config.partition()),
                () -> assertEquals(450L, config.offset()),
                () -> assertTrue(validator.validate(config).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is omitted from YAML")
        void shouldFailWhenPartitionIsMissingFromYaml() throws Exception {
            var yaml = "topic: topic-a\n"
                + "offset: 100\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionOffsetProperties.class);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when offset is omitted from YAML")
        void shouldFailWhenOffsetIsMissingFromYaml() throws Exception {
            var yaml = "topic: topic-a\n"
                + "partition: 0\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionOffsetProperties.class);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("offset")))
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should verify equality and hash code contracts across all branches")
        void shouldVerifyEqualsAndHashCode() {
            var config1 = new TopicPartitionOffsetProperties("topic-a", 200, 1000L);
            var config2 = new TopicPartitionOffsetProperties("topic-a", 200, 1000L);
            var configDifferentPartition = new TopicPartitionOffsetProperties("topic-a", 201, 1000L);
            var configDifferentOffset = new TopicPartitionOffsetProperties("topic-a", 200, 1001L);
            var configDifferentTopic = new TopicPartitionOffsetProperties("topic-b", 200, 1000L);

            assertAll(
                // Same instance
                () -> assertEquals(config1, config1),

                // Equal value
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),

                // Null
                () -> assertNotEquals(config1, null),

                // Different class
                () -> assertNotEquals(config1, "not-a-config-object"),

                // Different partition
                () -> assertNotEquals(config1, configDifferentPartition),

                // Different offset
                () -> assertNotEquals(config1, configDifferentOffset),

                // Different topic
                () -> assertNotEquals(config1, configDifferentTopic)
            );
        }

        @Test
        @DisplayName("Should return formatted string representation from toString")
        void shouldReturnFormattedStringFromToString() {
            var config = new TopicPartitionOffsetProperties("topic-a", 2, 500L);

            assertEquals(
                "TopicPartitionOffsetProperties{topic='topic-a', partition=2, offset=500}",
                config.toString()
            );
        }
    }
}