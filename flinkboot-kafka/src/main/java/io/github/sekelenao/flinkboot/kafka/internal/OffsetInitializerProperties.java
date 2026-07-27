package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalLong;

public interface OffsetInitializerProperties extends Serializable {

    KafkaOffsetInitializer startingOffsets();

    OptionalLong startingOffsetsTimestamp();

    List<TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets();

}
