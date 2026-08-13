package io.github.sekelenao.flinkboot.kafka.api.properties;

import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceTopicListProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceTopicPatternProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Kafka Configuration Edge Cases Tests")
class KafkaPropertiesEdgeCasesTest {

    @Test
    @DisplayName("Should test equals, hashCode, and toString on all Kafka DTOs")
    void shouldTestEqualsHashCodeToStringOnKafkaDtos() {
        var part1 = new TopicPartitionOffsetProperties("topic-1", 0, 100L);
        var part2 = new TopicPartitionOffsetProperties("topic-1", 0, 100L);
        var part3 = new TopicPartitionOffsetProperties("topic-2", 1, 200L);

        assertAll(
            () -> assertEquals(part1, part1),
            () -> assertEquals(part1, part2),
            () -> assertNotEquals(part1, part3),
            () -> assertNotEquals(part1, null),
            () -> assertNotEquals(part1, "other"),
            () -> assertEquals(part1.hashCode(), part2.hashCode()),
            () -> assertNotNull(part1.toString())
        );

        var listConfig1 = new KafkaSourceTopicListProperties("list-src", List.of("localhost:9092"), "group1", List.of("topic1"), KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v"));
        var listConfig2 = new KafkaSourceTopicListProperties("list-src", List.of("localhost:9092"), "group1", List.of("topic1"), KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v"));
        var listConfig3 = new KafkaSourceTopicListProperties("list-src-2", List.of("localhost:9092"), "group2", List.of("topic2"), KafkaOffsetInitializer.TIMESTAMP, 10000L, null, null);

        assertAll(
            () -> assertEquals(listConfig1, listConfig1),
            () -> assertEquals(listConfig1, listConfig2),
            () -> assertNotEquals(listConfig1, listConfig3),
            () -> assertNotEquals(listConfig1, null),
            () -> assertNotEquals(listConfig1, "other"),
            () -> assertEquals(listConfig1.hashCode(), listConfig2.hashCode()),
            () -> assertNotNull(listConfig1.toString())
        );

        var patternConfig1 = new KafkaSourceTopicPatternProperties("pattern-src", List.of("localhost:9092"), "group1", "^topic-.*$", KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v"));
        var patternConfig2 = new KafkaSourceTopicPatternProperties("pattern-src", List.of("localhost:9092"), "group1", "^topic-.*$", KafkaOffsetInitializer.EARLIEST, null, null, Map.of("k", "v"));
        var patternConfig3 = new KafkaSourceTopicPatternProperties("pattern-src-2", List.of("localhost:9092"), "group2", "^other-.*$", KafkaOffsetInitializer.TIMESTAMP, 10000L, null, null);

        assertAll(
            () -> assertEquals(patternConfig1, patternConfig1),
            () -> assertEquals(patternConfig1, patternConfig2),
            () -> assertNotEquals(patternConfig1, patternConfig3),
            () -> assertNotEquals(patternConfig1, null),
            () -> assertNotEquals(patternConfig1, "other"),
            () -> assertEquals(patternConfig1.hashCode(), patternConfig2.hashCode()),
            () -> assertNotNull(patternConfig1.toString())
        );

        var sinkConfig1 = new KafkaSinkProperties("sink-name", List.of("localhost:9092"), "topic1", KafkaDeliveryGuarantee.AT_LEAST_ONCE, null, Map.of("k", "v"));
        var sinkConfig2 = new KafkaSinkProperties("sink-name", List.of("localhost:9092"), "topic1", KafkaDeliveryGuarantee.AT_LEAST_ONCE, null, Map.of("k", "v"));
        var sinkConfig3 = new KafkaSinkProperties("sink-name-2", List.of("localhost:9092"), "topic2", KafkaDeliveryGuarantee.EXACTLY_ONCE, "tx-prefix", null);

        assertAll(
            () -> assertEquals(sinkConfig1, sinkConfig1),
            () -> assertEquals(sinkConfig1, sinkConfig2),
            () -> assertNotEquals(sinkConfig1, sinkConfig3),
            () -> assertNotEquals(sinkConfig1, null),
            () -> assertNotEquals(sinkConfig1, "other"),
            () -> assertEquals(sinkConfig1.hashCode(), sinkConfig2.hashCode()),
            () -> assertNotNull(sinkConfig1.toString())
        );
    }

    @Test
    @DisplayName("Should test getters with null or empty optional fields")
    void shouldTestGettersWithNullOrEmptyFields() {
        var nullSinkConfig = new KafkaSinkProperties("sink-name", List.of("localhost:9092"), "topic", null, "", null);
        var nullListConfig = new KafkaSourceTopicListProperties("list-src", List.of("localhost:9092"), "group", List.of("topic"), KafkaOffsetInitializer.EARLIEST, null, null, null);
        var nullPatternConfig = new KafkaSourceTopicPatternProperties("pattern-src", List.of("localhost:9092"), "group", "^pattern$", KafkaOffsetInitializer.EARLIEST, null, null, null);

        assertAll(
            () -> assertTrue(nullSinkConfig.properties().isEmpty()),
            () -> assertTrue(nullSinkConfig.deliveryGuarantee().isEmpty()),
            () -> assertTrue(nullSinkConfig.transactionalIdPrefix().isEmpty()),
            () -> assertTrue(nullListConfig.properties().isEmpty()),
            () -> assertEquals(KafkaOffsetInitializer.EARLIEST, nullListConfig.startingOffsets()),
            () -> assertTrue(nullListConfig.startingOffsetsTimestamp().isEmpty()),
            () -> assertTrue(nullListConfig.startingOffsetsPartitionOffsets().isEmpty()),
            () -> assertTrue(nullPatternConfig.properties().isEmpty())
        );
    }
}
