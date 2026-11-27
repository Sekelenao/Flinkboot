package com.flinkboot.kafka.api.properties.source;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TopicPartitionProperties")
class TopicPartitionPropertiesTest {

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
            var config = new TopicPartitionProperties("topic-a", 2);

            assertAll(
                () -> assertEquals("topic-a", config.topic()),
                () -> assertEquals(2, config.partition())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation with valid properties")
        void shouldPassValidationWithValidProperties() {
            var config = new TopicPartitionProperties("topic-a", 0);
            var violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Expected no validation violations");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should fail validation when topic is null, empty, or blank")
        void shouldFailWhenTopicIsNullOrEmptyOrBlank(String topic) {
            var config = new TopicPartitionProperties(topic, 0);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topic")))
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is null")
        void shouldFailWhenPartitionIsNull() {
            var config = new TopicPartitionProperties("topic-a", null);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is negative")
        void shouldFailWhenPartitionIsNegative() {
            var config = new TopicPartitionProperties("topic-a", -1);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
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
                + "partition: 3\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionProperties.class);

            assertAll(
                () -> assertEquals("events", config.topic()),
                () -> assertEquals(3, config.partition()),
                () -> assertTrue(validator.validate(config).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when partition is omitted from YAML")
        void shouldFailWhenPartitionIsMissingFromYaml() throws Exception {
            var yaml = "topic: topic-a\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionProperties.class);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("partition")))
            );
        }

        @Test
        @DisplayName("Should fail validation when topic is omitted from YAML")
        void shouldFailWhenTopicIsMissingFromYaml() throws Exception {
            var yaml = "partition: 0\n";

            var config = yamlMapper.readValue(yaml, TopicPartitionProperties.class);
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topic")))
            );
        }
    }
}
