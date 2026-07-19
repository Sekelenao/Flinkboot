package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.configuration.TopicPartitionOffsetConfiguration;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalLong;

public interface OffsetInitializerConfiguration extends Serializable {

    KafkaOffsetInitializer startingOffsets();

    OptionalLong startingOffsetsTimestamp();

    List<TopicPartitionOffsetConfiguration> startingOffsetsPartitionOffsets();

}
