package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TopicPartitionOffsetProperties")
class TopicPartitionOffsetPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Getters should return expected values")
    void testGetters() {
        var config = new TopicPartitionOffsetProperties("topic-a", 2, 500L);
        assertAll(
            () -> assertEquals("topic-a", config.topic()),
            () -> assertEquals(2, config.partition()),
            () -> assertEquals(500L, config.offset())
        );
    }

    @Test
    @DisplayName("Validation should pass with valid properties")
    void testValidationPasses() {
        var config = new TopicPartitionOffsetProperties("topic-a", 0, 100L);
        var violations = validator.validate(config);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation should fail when topic is null or blank")
    void testValidationFailsOnBlankOrNullTopic() {
        assertAll(
            () -> assertFalse(validator.validate(new TopicPartitionOffsetProperties(null, 0, 100L)).isEmpty()),
            () -> assertFalse(validator.validate(new TopicPartitionOffsetProperties("   ", 0, 100L)).isEmpty())
        );
    }

    @Test
    @DisplayName("Equals and HashCode should work correctly across all branches")
    void testEqualsAndHashCode() {
        var config1 = new TopicPartitionOffsetProperties("topic-a", 0, 100L);
        var config2 = new TopicPartitionOffsetProperties("topic-a", 0, 100L);
        var configDifferentPartition = new TopicPartitionOffsetProperties("topic-a", 1, 100L);
        var configDifferentOffset = new TopicPartitionOffsetProperties("topic-a", 0, 200L);
        var configDifferentTopic = new TopicPartitionOffsetProperties("topic-b", 0, 100L);

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
    @DisplayName("ToString should return the formatted string representation")
    void testToString() {
        var config = new TopicPartitionOffsetProperties("topic-a", 2, 500L);
        assertEquals("TopicPartitionOffsetProperties{topic='topic-a', partition=2, offset=500}", config.toString());
    }
}
