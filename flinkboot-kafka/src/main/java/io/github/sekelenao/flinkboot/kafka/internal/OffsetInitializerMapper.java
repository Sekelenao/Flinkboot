package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class OffsetInitializerMapper {

    private OffsetInitializerMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static OffsetsInitializer map(KafkaOffsetProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var strategy = properties.strategy();
        if (strategy == KafkaOffsetInitializer.OFFSETS) {
            return offsetsPerPartition(properties.partitions());
        }
        if (strategy == KafkaOffsetInitializer.TIMESTAMP) {
            return OffsetsInitializer.timestamp(properties.timestamp().orElseThrow());
        }
        return strategy.offsetsInitializer().orElseThrow();
    }

    private static OffsetsInitializer offsetsPerPartition(List<TopicPartitionOffsetProperties> partitionOffsets) {
        var offsetInitializerConfiguration = new HashMap<TopicPartition, Long>();
        for (var entry : partitionOffsets) {
            var topicPartition = new TopicPartition(entry.topic(), entry.partition());
            offsetInitializerConfiguration.put(topicPartition, entry.offset());
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }
}
