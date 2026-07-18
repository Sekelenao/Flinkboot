package io.github.sekelenao.flinkboot.kafka.internal;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;

public final class OffsetInitializerMapper {

    private OffsetInitializerMapper(){
        throw new AssertionError("You cannot instantiate this class");
    }

    public static OffsetsInitializer map(OffsetInitializerConfiguration configuration){
        var offset = configuration.startingOffsets();
        switch (offset){
            case EARLIEST: case LATEST: case COMMITTED: case COMMITTED_EARLIEST: case COMMITTED_LATEST:
                return offset.offsetsInitializer();
            case OFFSETS:
                return offsetsPerPartition(configuration);
            case TIMESTAMP:
                return timestampOffsets(configuration);
            default:
                throw new IllegalStateException("Unknown offset initializer type: " + offset);
        }
    }

    private static OffsetsInitializer offsetsPerPartition(OffsetInitializerConfiguration configuration){
        var offsetsList = configuration.startingOffsetsPartitionOffsets();
        if (offsetsList.isEmpty()) {
            throw new IllegalArgumentException("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS");
        }
        var offsetInitializerConfiguration = new HashMap<TopicPartition, Long>();
        for (var entry : offsetsList){
            var topicPartition = new TopicPartition(entry.topic(), entry.partition());
            offsetInitializerConfiguration.put(topicPartition, entry.offset());
        }
        return OffsetsInitializer.offsets(offsetInitializerConfiguration);
    }

    private static OffsetsInitializer timestampOffsets(OffsetInitializerConfiguration configuration){
        var optionalTimestamp = configuration.startingOffsetsTimestamp();
        if (optionalTimestamp.isEmpty()) {
            throw new IllegalArgumentException("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP");
        }
        return OffsetsInitializer.timestamp(optionalTimestamp.getAsLong());
    }

}
