package io.github.sekelenao.flinkboot.kafka.internal;

import java.util.HashMap;
import java.util.Objects;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
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
            var previousOffset = offsetInitializerConfiguration.put(topicPartition, entry.offset());

            if (previousOffset != null) {
                throw new IllegalArgumentException(
                    String.format("Duplicate partition offset configuration for topic '%s' and partition %d",
                        entry.topic(), entry.partition())
                );
            }
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }

    private static OffsetsInitializer timestampOffsets(KafkaSourceProperties properties) {
        return OffsetsInitializer.timestamp(properties.startingOffsetsTimestamp().orElseThrow());
    }
}
