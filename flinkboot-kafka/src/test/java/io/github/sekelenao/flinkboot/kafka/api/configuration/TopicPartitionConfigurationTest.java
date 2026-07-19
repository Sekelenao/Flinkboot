package io.github.sekelenao.flinkboot.kafka.api.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("TopicPartitionConfiguration")
class TopicPartitionConfigurationTest {

    @Test
    @DisplayName("Equals and HashCode should work correctly across all branches")
    void testEqualsAndHashCode() {
        var config1 = new TopicPartitionConfiguration("topic-a", 0, 100L);
        var config2 = new TopicPartitionConfiguration("topic-a", 0, 100L);
        var configDifferentPartition = new TopicPartitionConfiguration("topic-a", 1, 100L);
        var configDifferentOffset = new TopicPartitionConfiguration("topic-a", 0, 200L);
        var configDifferentTopic = new TopicPartitionConfiguration("topic-b", 0, 100L);

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
        var config = new TopicPartitionConfiguration("topic-a", 2, 500L);
        assertEquals("topic-a-2:500", config.toString());
    }
}
