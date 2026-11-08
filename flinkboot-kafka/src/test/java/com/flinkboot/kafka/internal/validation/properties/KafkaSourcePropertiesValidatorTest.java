package com.flinkboot.kafka.internal.validation.properties;

import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import com.flinkboot.kafka.api.properties.source.TopicPartitionProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("KafkaSourcePropertiesValidator")
class KafkaSourcePropertiesValidatorTest {

    private static final KafkaOffsetProperties DEFAULT_STARTING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null);

    private static final KafkaOffsetProperties DEFAULT_STOPPING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);

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
                null,
                List.of("topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
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
        @DisplayName("Should pass when valid topics are configured without pattern or partitions")
        void shouldPassWhenTopicsConfiguredWithoutPattern() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when valid pattern is configured without topics or partitions")
        void shouldPassWhenPatternConfiguredWithoutTopics() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                "my-topic-.*",
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when valid partitions are configured without topics or pattern")
        void shouldPassWhenPartitionsConfiguredWithoutTopicsOrPattern() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                null,
                List.of(new TopicPartitionProperties("my-topic", 0)),
                DEFAULT_STARTING_OFFSETS,
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
                null,
                List.of("my-topic"),
                "my-topic-.*",
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("topic-pattern");
        }

        @Test
        @DisplayName("Should fail when both topics and partitions are configured")
        void shouldFailWhenBothTopicsAndPartitionsConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                List.of(new TopicPartitionProperties("my-topic", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("partitions");
        }

        @Test
        @DisplayName("Should fail when both topic-pattern and partitions are configured")
        void shouldFailWhenBothTopicPatternAndPartitionsConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                "my-topic-.*",
                List.of(new TopicPartitionProperties("my-topic", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("partitions");
        }

        @Test
        @DisplayName("Should fail when all three topics, topic-pattern, and partitions are configured")
        void shouldFailWhenAllThreeConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                "my-topic-.*",
                List.of(new TopicPartitionProperties("my-topic", 0)),
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("partitions");
        }

        @Test
        @DisplayName("Should fail when none of topics, topic-pattern, or partitions are configured")
        void shouldFailWhenNeitherTopicsNorPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("topics");
        }

        @Test
        @DisplayName("Should fail when both topics and empty topic-pattern are configured")
        void shouldFailWhenBothTopicsAndEmptyTopicPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                "",
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("topic-pattern");
        }

        @Test
        @DisplayName("Should pass when boundedness is BOUNDED and stopping-offsets is present")
        void shouldPassWhenBoundedWithStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when boundedness is BOUNDED and stopping-offsets is absent")
        void shouldFailWhenBoundedWithoutStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "stopping-offsets is required when boundedness is BOUNDED"
            )).addPropertyNode("stopping-offsets");
        }

        @Test
        @DisplayName("Should pass when boundedness is UNBOUNDED and stopping-offsets is absent")
        void shouldPassWhenUnboundedWithoutStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.UNBOUNDED,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when boundedness is UNBOUNDED and stopping-offsets is present (finite streaming)")
        void shouldPassWhenUnboundedWithStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.UNBOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when stopping-offsets is present and boundedness is absent")
        void shouldFailWhenStoppingOffsetsPresentAndBoundednessAbsent() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                DEFAULT_STARTING_OFFSETS,
                null,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate(
                "boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured"
            );
        }
    }
}
