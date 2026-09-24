package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("KafkaSourcePropertiesValidator")
class KafkaSourcePropertiesValidatorTest {

    @Nested
    @DisplayName("Preconditions")
    class Preconditions {

        @Test
        @DisplayName("Should throw NullPointerException when properties is null")
        void shouldThrowWhenPropertiesIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            var exception = assertThrows(NullPointerException.class, () -> KafkaSourcePropertiesValidator.validate(null, context));
            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("topic"),
                null,
                null,
                null,
                null,
                Map.of()
            );
            var exception = assertThrows(NullPointerException.class, () -> KafkaSourcePropertiesValidator.validate(props, null));
            assertEquals("context must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass when valid topics are configured without pattern")
        void shouldPassWhenTopicsConfiguredWithoutPattern() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when valid pattern is configured without topics")
        void shouldPassWhenPatternConfiguredWithoutTopics() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                "my-topic-.*",
                KafkaOffsetInitializer.LATEST,
                null,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when both topics and topic-pattern are configured")
        void shouldFailWhenBothTopicsAndPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                "my-topic-.*",
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when neither topics nor topic-pattern are configured")
        void shouldFailWhenNeitherTopicsNorPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when both topics and empty topic-pattern are configured")
        void shouldFailWhenBothTopicsAndEmptyTopicPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                "",
                KafkaOffsetInitializer.EARLIEST,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when startingOffsets is null")
        void shouldPassWhenStartingOffsetsIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                null,
                null,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when TIMESTAMP mode is used with timestamp")
        void shouldPassWhenTimestampModeWithTimestamp() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                123456789L,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when TIMESTAMP mode is used without timestamp")
        void shouldFailWhenTimestampModeWithoutTimestamp() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when TIMESTAMP mode is used with partition offsets")
        void shouldFailWhenTimestampModeWithPartitionOffsets() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var partitionOffset = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                123456789L,
                List.of(partitionOffset),
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when OFFSETS mode is used with partition offsets")
        void shouldPassWhenOffsetsModeWithPartitionOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var partitionOffset = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                List.of(partitionOffset),
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when OFFSETS mode is used without partition offsets")
        void shouldFailWhenOffsetsModeWithoutPartitionOffsets() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when OFFSETS mode is used with timestamp")
        void shouldFailWhenOffsetsModeWithTimestamp() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var partitionOffset = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                123456789L,
                List.of(partitionOffset),
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when standard offset strategy has timestamp")
        void shouldFailWhenStandardStrategyHasTimestamp(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                strategy,
                123456789L,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when standard offset strategy has partition offsets")
        void shouldFailWhenStandardStrategyHasPartitionOffsets(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var partitionOffset = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                strategy,
                null,
                List.of(partitionOffset),
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }
    }
}
