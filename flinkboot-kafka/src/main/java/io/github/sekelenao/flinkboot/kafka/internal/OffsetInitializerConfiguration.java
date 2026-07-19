package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.configuration.source.TopicPartitionOffsetConfiguration;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalLong;

public interface OffsetInitializerConfiguration extends Serializable {

    KafkaOffsetInitializer startingOffsets();

    OptionalLong startingOffsetsTimestamp();

    List<TopicPartitionOffsetConfiguration> startingOffsetsPartitionOffsets();

}
