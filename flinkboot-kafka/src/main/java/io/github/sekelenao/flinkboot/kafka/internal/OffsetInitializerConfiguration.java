package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.configuration.TopicPartitionConfiguration;

import java.util.List;
import java.util.Optional;

public interface OffsetInitializerConfiguration {

    KafkaOffsetInitializer startingOffsets();

    Optional<Long> startingOffsetsTimestamp();

    Optional<List<TopicPartitionConfiguration>> startingOffsetsPartitionOffsets();

}
