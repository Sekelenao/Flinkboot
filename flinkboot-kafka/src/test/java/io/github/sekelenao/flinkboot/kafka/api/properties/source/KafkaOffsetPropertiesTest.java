package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaOffsetProperties")
class KafkaOffsetPropertiesTest {

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
        @DisplayName("Should successfully construct with EARLIEST strategy and default empty values")
        void shouldConstructWithEarliestStrategy() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, props.strategy()),
                () -> assertEquals(OptionalLong.empty(), props.timestamp()),
                () -> assertTrue(props.partitions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should successfully construct with TIMESTAMP strategy and timestamp")
        void shouldConstructWithTimestampStrategy() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, props.strategy()),
                () -> assertEquals(OptionalLong.of(1689717600000L), props.timestamp()),
                () -> assertTrue(props.partitions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should successfully construct with OFFSETS strategy and partitions")
        void shouldConstructWithOffsetsStrategy() {
            var partition = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition));

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, props.strategy()),
                () -> assertEquals(OptionalLong.empty(), props.timestamp()),
                () -> assertEquals(List.of(partition), props.partitions())
            );
        }

        @Test
        @DisplayName("Should return unmodifiable list of partitions")
        void shouldReturnUnmodifiablePartitions() {
            var partition = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var partitions = new ArrayList<TopicPartitionOffsetProperties>();
            partitions.add(partition);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, partitions);

            var result = props.partitions();
            assertThrows(UnsupportedOperationException.class, () -> result.add(new TopicPartitionOffsetProperties("other", 1, 200L)));
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should pass validation for standard strategies without timestamp and partitions")
        void shouldPassForStandardStrategies(KafkaOffsetInitializer strategy) {
            var props = new KafkaOffsetProperties(strategy, null, null);
            var violations = validator.validate(props);

            assertTrue(violations.isEmpty(), "Expected no validation violations");
        }

        @Test
        @DisplayName("Should pass validation for TIMESTAMP strategy with valid positive timestamp")
        void shouldPassForTimestampStrategy() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null);
            var violations = validator.validate(props);

            assertTrue(violations.isEmpty(), "Expected no validation violations");
        }

        @Test
        @DisplayName("Should pass validation for OFFSETS strategy with non-empty partitions")
        void shouldPassForOffsetsStrategy() {
            var partition = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition));
            var violations = validator.validate(props);

            assertTrue(violations.isEmpty(), "Expected no validation violations");
        }

        @Test
        @DisplayName("Should fail validation when strategy is null")
        void shouldFailWhenStrategyIsNull() {
            var props = new KafkaOffsetProperties(null, null, null);
            var violations = validator.validate(props);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("strategy")))
            );
        }

        @Test
        @DisplayName("Should fail validation when TIMESTAMP strategy has null timestamp")
        void shouldFailWhenTimestampStrategyHasNullTimestamp() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, null, null);
            var violations = validator.validate(props);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("timestamp")
                    && v.getMessage().contains("timestamp is required when strategy is TIMESTAMP")))
            );
        }

        @Test
        @DisplayName("Should fail validation when TIMESTAMP strategy has negative timestamp")
        void shouldFailWhenTimestampStrategyHasNegativeTimestamp() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, -1L, null);
            var violations = validator.validate(props);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("timestamp")))
            );
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail validation when non-TIMESTAMP strategy has timestamp specified")
        void shouldFailWhenNonTimestampStrategyHasTimestamp(KafkaOffsetInitializer strategy) {
            var props = new KafkaOffsetProperties(
                strategy,
                1000L,
                strategy == KafkaOffsetInitializer.OFFSETS ? List.of(new TopicPartitionOffsetProperties("t", 0, 0L)) : null
            );
            var violations = validator.validate(props);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("timestamp")
                    && v.getMessage().contains("timestamp must not be specified when strategy is " + strategy)))
            );
        }

        @Test
        @DisplayName("Should fail validation when OFFSETS strategy has null partitions")
        void shouldFailWhenOffsetsStrategyHasNullPartitions() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, null);
            var violations = validator.validate(props);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("partitions is required when strategy is OFFSETS")))
            );
        }

        @Test
        @DisplayName("Should fail validation when OFFSETS strategy has empty partitions")
        void shouldFailWhenOffsetsStrategyHasEmptyPartitions() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of());
            var violations = validator.validate(props);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("partitions is required when strategy is OFFSETS")))
            );
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"OFFSETS"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail validation when non-OFFSETS strategy has partitions specified")
        void shouldFailWhenNonOffsetsStrategyHasPartitions(KafkaOffsetInitializer strategy) {
            var partition = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var props = new KafkaOffsetProperties(
                strategy,
                strategy == KafkaOffsetInitializer.TIMESTAMP ? 1000L : null,
                List.of(partition)
            );
            var violations = validator.validate(props);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("partitions")
                    && v.getMessage().contains("partitions must not be specified when strategy is " + strategy)))
            );
        }

        @Test
        @DisplayName("Should fail validation when partitions contain invalid element")
        void shouldFailWhenPartitionElementIsInvalid() {
            var invalidPartition = new TopicPartitionOffsetProperties(null, 0, 100L);
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(invalidPartition));
            var violations = validator.validate(props);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("partitions")))
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should deserialize YAML with minimal EARLIEST strategy")
        void shouldDeserializeEarliestYaml() throws Exception {
            var yaml = "strategy: EARLIEST\n";
            var props = yamlMapper.readValue(yaml, KafkaOffsetProperties.class);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.EARLIEST, props.strategy()),
                () -> assertEquals(OptionalLong.empty(), props.timestamp()),
                () -> assertTrue(props.partitions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize YAML with TIMESTAMP strategy and timestamp")
        void shouldDeserializeTimestampYaml() throws Exception {
            var yaml = "strategy: TIMESTAMP\n"
                + "timestamp: 1689717600000\n";
            var props = yamlMapper.readValue(yaml, KafkaOffsetProperties.class);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.TIMESTAMP, props.strategy()),
                () -> assertEquals(OptionalLong.of(1689717600000L), props.timestamp()),
                () -> assertTrue(props.partitions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize YAML with OFFSETS strategy and partitions")
        void shouldDeserializeOffsetsYaml() throws Exception {
            var yaml = "strategy: OFFSETS\n"
                + "partitions:\n"
                + "  - topic: topic-a\n"
                + "    partition: 0\n"
                + "    offset: 100\n";
            var props = yamlMapper.readValue(yaml, KafkaOffsetProperties.class);

            assertAll(
                () -> assertEquals(KafkaOffsetInitializer.OFFSETS, props.strategy()),
                () -> assertEquals(OptionalLong.empty(), props.timestamp()),
                () -> assertEquals(1, props.partitions().size()),
                () -> assertEquals("topic-a", props.partitions().get(0).topic()),
                () -> assertEquals(0, props.partitions().get(0).partition()),
                () -> assertEquals(100L, props.partitions().get(0).offset())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should verify reflexive, symmetric equality and hashCode")
        void shouldVerifyEquality() {
            var partition = new TopicPartitionOffsetProperties("my-topic", 0, 100L);
            var a = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition));
            var b = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition));

            assertAll(
                () -> assertEquals(a, a),
                () -> assertEquals(a, b),
                () -> assertEquals(b, a),
                () -> assertEquals(a.hashCode(), b.hashCode())
            );
        }

        @Test
        @DisplayName("Should verify inequality when fields differ")
        void shouldVerifyInequality() {
            var a = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 100L, List.of(new TopicPartitionOffsetProperties("t", 0, 0L)));
            var diffStrategy = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, 100L, List.of(new TopicPartitionOffsetProperties("t", 0, 0L)));
            var diffTimestamp = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 200L, List.of(new TopicPartitionOffsetProperties("t", 0, 0L)));
            var diffPartitions = new KafkaOffsetProperties(
                KafkaOffsetInitializer.TIMESTAMP, 100L, List.of(new TopicPartitionOffsetProperties("t", 1, 0L)));

            assertAll(
                () -> assertNotEquals(a, diffStrategy),
                () -> assertNotEquals(a, diffTimestamp),
                () -> assertNotEquals(a, diffPartitions),
                () -> assertNotEquals(a, null),
                () -> assertNotEquals(a, "other type")
            );
        }

        @Test
        @DisplayName("Should produce meaningful toString output")
        void shouldProduceMeaningfulToString() {
            var props = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null);
            var str = props.toString();

            assertAll(
                () -> assertTrue(str.contains("strategy=EARLIEST")),
                () -> assertTrue(str.contains("KafkaOffsetProperties"))
            );
        }
    }
}
