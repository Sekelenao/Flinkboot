package com.flinkboot.kafka.internal.validation.properties.offset;

import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetResetStrategy;
import com.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("KafkaOffsetPropertiesValidator")
class KafkaOffsetPropertiesValidatorTest {

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should return true when strategy is null (delegating to Jakarta @NotNull)")
        void shouldReturnTrueWhenStrategyIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaOffsetProperties(null, null, null, null);

            assertTrue(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should return true for valid standard strategy without timestamp and partitions")
        void shouldReturnTrueForValidStandardStrategy(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaOffsetProperties(strategy, null, null, null);

            assertTrue(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return false when TIMESTAMP strategy is missing timestamp")
        void shouldReturnFalseWhenTimestampStrategyIsMissingTimestamp() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, null, null, null);

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true for valid TIMESTAMP strategy with timestamp")
        void shouldReturnTrueForValidTimestamp() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1000L, null, null);

            assertTrue(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return false when OFFSETS strategy is missing partitions")
        void shouldReturnFalseWhenOffsetsStrategyIsMissingPartitions() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, null, KafkaOffsetResetStrategy.EARLIEST);

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return false when OFFSETS strategy is missing reset-strategy")
        void shouldReturnFalseWhenOffsetsStrategyIsMissingResetStrategy() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var partition = new TopicPartitionOffsetProperties("topic", 0, 10L);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition), null);

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true for valid OFFSETS strategy with partitions and reset-strategy")
        void shouldReturnTrueForValidOffsets() {
            var context = mock(ConstraintValidatorContext.class);
            var partition = new TopicPartitionOffsetProperties("topic", 0, 10L);
            var props = new KafkaOffsetProperties(
                KafkaOffsetInitializer.OFFSETS, null, List.of(partition), KafkaOffsetResetStrategy.EARLIEST
            );

            assertTrue(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should return false when standard strategy has timestamp")
        void shouldReturnFalseWhenStandardStrategyHasTimestamp(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaOffsetProperties(strategy, 1000L, null, null);

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should return false when standard strategy has partitions")
        void shouldReturnFalseWhenStandardStrategyHasPartitions(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var partition = new TopicPartitionOffsetProperties("topic", 0, 10L);
            var props = new KafkaOffsetProperties(strategy, null, List.of(partition), null);

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should return false when non-OFFSETS strategy has reset-strategy")
        void shouldReturnFalseWhenNonOffsetsStrategyHasResetStrategy(KafkaOffsetInitializer strategy) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaOffsetProperties(
                strategy,
                strategy == KafkaOffsetInitializer.TIMESTAMP ? 1000L : null,
                null,
                KafkaOffsetResetStrategy.EARLIEST
            );

            assertFalse(KafkaOffsetPropertiesValidator.validate(props, context));
        }
    }
}
