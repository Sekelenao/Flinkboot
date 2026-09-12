package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Objects;

public final class OffsetInitializerMapper {

    private OffsetInitializerMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static OffsetsInitializer map(KafkaSourceProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var offset = properties.startingOffsets();
        if (offset == KafkaOffsetInitializer.OFFSETS) {
            return offsetsPerPartition(properties);
        }
        if (offset == KafkaOffsetInitializer.TIMESTAMP) {
            return timestampOffsets(properties);
        }
        return offset.offsetsInitializer();
    }

    private static OffsetsInitializer offsetsPerPartition(KafkaSourceProperties properties) {
        var offsetInitializerConfiguration = new HashMap<TopicPartition, Long>();
        for (var entry : properties.startingOffsetsPartitionOffsets()) {
            var topicPartition = new TopicPartition(entry.topic(), entry.partition());
            offsetInitializerConfiguration.put(topicPartition, entry.offset());
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }

    private static OffsetsInitializer timestampOffsets(KafkaSourceProperties properties) {
        return OffsetsInitializer.timestamp(properties.startingOffsetsTimestamp().orElseThrow());
    }
}
