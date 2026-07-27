package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;

public final class OffsetInitializerMapper {

    private OffsetInitializerMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static OffsetsInitializer map(OffsetInitializerProperties configuration) {
        var offset = configuration.startingOffsets();
        if (offset == KafkaOffsetInitializer.OFFSETS) {
            return offsetsPerPartition(configuration);
        }
        if (offset == KafkaOffsetInitializer.TIMESTAMP) {
            return timestampOffsets(configuration);
        }
        return offset.offsetsInitializer();
    }

    private static OffsetsInitializer offsetsPerPartition(OffsetInitializerProperties configuration) {
        var offsetInitializerConfiguration = new HashMap<TopicPartition, Long>();
        for (var entry : configuration.startingOffsetsPartitionOffsets()) {
            var topicPartition = new TopicPartition(entry.topic(), entry.partition());
            offsetInitializerConfiguration.put(topicPartition, entry.offset());
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }

    private static OffsetsInitializer timestampOffsets(OffsetInitializerProperties configuration) {
        return OffsetsInitializer.timestamp(configuration.startingOffsetsTimestamp().orElseThrow());
    }
}
