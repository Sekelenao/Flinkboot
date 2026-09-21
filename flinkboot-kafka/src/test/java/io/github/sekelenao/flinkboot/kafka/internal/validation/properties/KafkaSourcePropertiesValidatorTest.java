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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("topicPattern")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Cannot configure both 'topics' and 'topic-pattern'");
            verify(builder).addPropertyNode("topicPattern");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should fail when neither topics nor topic-pattern are configured")
        void shouldFailWhenNeitherTopicsNorPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("topics")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Either 'topics' or 'topic-pattern' must be specified");
            verify(builder).addPropertyNode("topics");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should fail when both topics and empty topic-pattern are configured")
        void shouldFailWhenBothTopicsAndEmptyTopicPatternConfigured() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("topicPattern")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Cannot configure both 'topics' and 'topic-pattern'");
            verify(builder).addPropertyNode("topicPattern");
            verify(nodeBuilder).addConstraintViolation();
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
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsTimestamp")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP");
            verify(builder).addPropertyNode("startingOffsetsTimestamp");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should fail when TIMESTAMP mode is used with partition offsets")
        void shouldFailWhenTimestampModeWithPartitionOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsPartitionOffsets")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-partition-offsets must not be specified when starting-offsets is TIMESTAMP");
            verify(builder).addPropertyNode("startingOffsetsPartitionOffsets");
            verify(nodeBuilder).addConstraintViolation();
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
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsPartitionOffsets")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS");
            verify(builder).addPropertyNode("startingOffsetsPartitionOffsets");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should fail when OFFSETS mode is used with timestamp")
        void shouldFailWhenOffsetsModeWithTimestamp() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsTimestamp")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-timestamp must not be specified when starting-offsets is OFFSETS");
            verify(builder).addPropertyNode("startingOffsetsTimestamp");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should fail with partition-offsets violation when OFFSETS mode has both missing partition-offsets and present timestamp")
        void shouldFailWhenOffsetsModeWithoutPartitionOffsetsAndWithTimestamp() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsPartitionOffsets")).thenReturn(nodeBuilder);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                List.of("my-topic"),
                null,
                KafkaOffsetInitializer.OFFSETS,
                123456789L,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS");
            verify(builder).addPropertyNode("startingOffsetsPartitionOffsets");
            verify(nodeBuilder).addConstraintViolation();
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when standard offset strategy has timestamp")
        void shouldFailWhenStandardStrategyHasTimestamp(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsTimestamp")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-timestamp must not be specified when starting-offsets is " + strategy);
            verify(builder).addPropertyNode("startingOffsetsTimestamp");
            verify(nodeBuilder).addConstraintViolation();
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when standard offset strategy has partition offsets")
        void shouldFailWhenStandardStrategyHasPartitionOffsets(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("startingOffsetsPartitionOffsets")).thenReturn(nodeBuilder);

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
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("starting-offsets-partition-offsets must not be specified when starting-offsets is " + strategy);
            verify(builder).addPropertyNode("startingOffsetsPartitionOffsets");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should report both subscription and starting-offsets violations simultaneously without short-circuiting")
        void shouldReportBothSubscriptionAndStartingOffsetsViolationsSimultaneously() {
            var context = mock(ConstraintValidatorContext.class);
            var topicBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var topicNode = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
            var offsetBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var offsetNode = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate("Either 'topics' or 'topic-pattern' must be specified")).thenReturn(topicBuilder);
            when(topicBuilder.addPropertyNode("topics")).thenReturn(topicNode);

            when(context.buildConstraintViolationWithTemplate("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP")).thenReturn(offsetBuilder);
            when(offsetBuilder.addPropertyNode("startingOffsetsTimestamp")).thenReturn(offsetNode);

            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                null,
                KafkaOffsetInitializer.TIMESTAMP,
                null,
                null,
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));

            verify(context, times(2)).disableDefaultConstraintViolation();
            verify(topicNode).addConstraintViolation();
            verify(offsetNode).addConstraintViolation();
        }
    }
}
