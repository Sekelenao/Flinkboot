package io.github.sekelenao.flinkboot.kafka.api.configuration.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;

public final class TopicPartitionOffsetConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String topic;

    @PositiveOrZero
    private final int partition;

    @PositiveOrZero
    private final long offset;

    @JsonCreator
    public TopicPartitionOffsetConfiguration(
        @JsonProperty("topic") String topic,
        @JsonProperty("partition") int partition,
        @JsonProperty("offset") long offset
    ) {
        this.topic = Objects.requireNonNull(topic);
        this.partition = partition;
        this.offset = offset;
    }

    public String topic() {
        return topic;
    }

    public int partition() {
        return partition;
    }

    public long offset() {
        return offset;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof TopicPartitionOffsetConfiguration)) {
            return false;
        }
        var otherTopicPartitionConfiguration = (TopicPartitionOffsetConfiguration) other;
        return Objects.equals(topic, otherTopicPartitionConfiguration.topic)
            && partition == otherTopicPartitionConfiguration.partition
            && offset == otherTopicPartitionConfiguration.offset;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(topic, partition, offset);
    }

    @Override
    @Generated
    public String toString() {
        return "TopicPartitionConfiguration{" +
            "topic='" + topic + '\'' +
            ", partition=" + partition +
            ", offset=" + offset +
            '}';
    }

}
