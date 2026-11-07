package com.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;

/**
 * Configuration properties for a specific Kafka topic partition.
 */
public final class TopicPartitionProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String topic;

    @NotNull
    @PositiveOrZero
    private final Integer partition;

    /**
     * Creates a new {@code TopicPartitionProperties} instance.
     *
     * @param topic     Kafka topic name
     * @param partition partition index (0-indexed)
     */
    @JsonCreator
    public TopicPartitionProperties(
        @JsonProperty("topic") String topic,
        @JsonProperty("partition") Integer partition
    ) {
        this.topic = topic;
        this.partition = partition;
    }

    /**
     * Returns the topic name.
     *
     * @return the topic name
     */
    public String topic() {
        return topic;
    }

    /**
     * Returns the partition index.
     *
     * @return the partition index
     */
    public int partition() {
        return partition;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof TopicPartitionProperties)) {
            return false;
        }
        var o = (TopicPartitionProperties) other;
        return Objects.equals(topic, o.topic)
            && Objects.equals(partition, o.partition);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(topic, partition);
    }

    @Override
    @Generated
    public String toString() {
        return "TopicPartitionProperties{" +
            "topic='" + topic + '\'' +
            ", partition=" + partition +
            '}';
    }
}
