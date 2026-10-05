package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
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

@DisplayName("KafkaSourcePropertiesValidator")
class KafkaSourcePropertiesValidatorTest {

    private static final KafkaOffsetProperties DEFAULT_STARTING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null);

    private static final KafkaOffsetProperties DEFAULT_STOPPING_OFFSETS =
        new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null);

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
        @DisplayName("Should pass when valid topics are configured without pattern")
        void shouldPassWhenTopicsConfiguredWithoutPattern() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
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
                List.of("my-topic"),
                "my-topic-.*",
                DEFAULT_STARTING_OFFSETS,
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
                DEFAULT_STARTING_OFFSETS,
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
                DEFAULT_STARTING_OFFSETS,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when boundedness is BOUNDED and stopping-offsets is present")
        void shouldPassWhenBoundedWithStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
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
                List.of("my-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.BOUNDED,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when boundedness is UNBOUNDED and stopping-offsets is absent")
        void shouldPassWhenUnboundedWithoutStoppingOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
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
                List.of("my-topic"),
                null,
                DEFAULT_STARTING_OFFSETS,
                KafkaBoundedness.UNBOUNDED,
                DEFAULT_STOPPING_OFFSETS,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }
    }
}
