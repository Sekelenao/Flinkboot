package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("KafkaSourcePropertiesValidator")
class KafkaSourcePropertiesValidatorTest {

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should return true when all Kafka source rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                "group",
                null,
                List.of("my-topic"),
                null,
                null,
                new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null),
                KafkaBoundedness.UNBOUNDED,
                null,
                Map.of()
            );

            assertTrue(KafkaSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should accumulate all violations when multiple Kafka source rules fail simultaneously")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSourceProperties(
                "source",
                List.of("localhost:9092"),
                null,
                null,
                List.of("my-topic"),
                "my-topic-.*",
                null,
                new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 200L, null, null),
                null,
                new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, null, null),
                Map.of()
            );

            assertFalse(KafkaSourcePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "Exactly one of 'topics', 'topic-pattern', or 'partitions' must be specified"
            )).addPropertyNode("topic-pattern");
            verify(context.buildConstraintViolationWithTemplate(
                "boundedness must be explicitly specified (BOUNDED or UNBOUNDED) when stopping-offsets is configured"
            )).addPropertyNode("boundedness");
            verify(context.buildConstraintViolationWithTemplate(
                "stopping-offsets.timestamp must be greater than or equal to starting-offsets.timestamp"
            )).addPropertyNode("stopping-offsets");
        }
    }
}
