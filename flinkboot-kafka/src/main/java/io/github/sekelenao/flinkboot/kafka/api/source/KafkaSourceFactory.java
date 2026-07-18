package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicListConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicPatternConfiguration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class KafkaSourceFactory {

    private KafkaSourceFactory(){
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicListConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(schema);

        var additionalProperties = new Properties();
        config.properties().ifPresent(additionalProperties::putAll);

        var startingOffsets = getStartingOffsets(
            config.startingOffsets(),
            config.startingOffsetsTimestamp().orElse(null),
            config.startingOffsetsPartitionOffsets().orElse(null)
        );

        return KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setTopics(config.topics())
            .setStartingOffsets(startingOffsets)
            .setProperties(additionalProperties)
            .setDeserializer(schema);
    }

    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicPatternConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(schema);

        var additionalProperties = new Properties();
        config.properties().ifPresent(additionalProperties::putAll);

        var startingOffsets = getStartingOffsets(
            config.startingOffsets(),
            config.startingOffsetsTimestamp().orElse(null),
            config.startingOffsetsPartitionOffsets().orElse(null)
        );

        return KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setTopicPattern(config.topicPattern())
            .setStartingOffsets(startingOffsets)
            .setProperties(additionalProperties)
            .setDeserializer(schema);
    }

    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicListConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicPatternConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

    private static OffsetsInitializer getStartingOffsets(
        KafkaOffsetInitializer startingOffsets,
        Long timestamp,
        Map<String, Long> partitionOffsets
    ) {
        if (startingOffsets == KafkaOffsetInitializer.TIMESTAMP) {
            if (timestamp == null) {
                throw new IllegalArgumentException("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP");
            }
            return OffsetsInitializer.timestamp(timestamp);
        }
        if (startingOffsets == KafkaOffsetInitializer.OFFSETS) {
            if (partitionOffsets == null || partitionOffsets.isEmpty()) {
                throw new IllegalArgumentException("starting-offsets-partition-offsets is required when starting-offsets is OFFSETS");
            }
            var topicPartitions = new HashMap<TopicPartition, Long>();
            partitionOffsets.forEach((key, val) -> {
                int lastDash = key.lastIndexOf('-');
                if (lastDash == -1) {
                    throw new IllegalArgumentException("Invalid partition offset key format. Expected 'topic-partition' (e.g. my-topic-0), got: " + key);
                }
                String topic = key.substring(0, lastDash);
                int partition;
                try {
                    partition = Integer.parseInt(key.substring(lastDash + 1));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid partition number in key: " + key, e);
                }
                topicPartitions.put(new TopicPartition(topic, partition), val);
            });
            return OffsetsInitializer.offsets(topicPartitions);
        }
        return startingOffsets.offsetsInitializer();
    }

}
