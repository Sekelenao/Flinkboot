package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaOffsetInitializer;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;

public final class OffsetInitializerMapper {

    private OffsetInitializerMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static OffsetsInitializer map(OffsetInitializerConfiguration configuration) {
        var offset = configuration.startingOffsets();
        if (offset == KafkaOffsetInitializer.OFFSETS) {
            return offsetsPerPartition(configuration);
        }
        if (offset == KafkaOffsetInitializer.TIMESTAMP) {
            return timestampOffsets(configuration);
        }
        return offset.offsetsInitializer();
    }

    private static OffsetsInitializer offsetsPerPartition(OffsetInitializerConfiguration configuration) {
        var offsetInitializerConfiguration = new HashMap<TopicPartition, Long>();
        for (var entry : configuration.startingOffsetsPartitionOffsets()) {
            var topicPartition = new TopicPartition(entry.topic(), entry.partition());
            offsetInitializerConfiguration.put(topicPartition, entry.offset());
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }

    private static OffsetsInitializer timestampOffsets(OffsetInitializerConfiguration configuration) {
        return OffsetsInitializer.timestamp(configuration.startingOffsetsTimestamp().orElseThrow());
    }
}
