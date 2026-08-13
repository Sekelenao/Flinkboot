package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;

public final class TopicPartitionOffsetProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String topic;

    @PositiveOrZero
    private final int partition;

    @PositiveOrZero
    private final long offset;

    @JsonCreator
    public TopicPartitionOffsetProperties(
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
        if (!(other instanceof TopicPartitionOffsetProperties)) {
            return false;
        }
        var o = (TopicPartitionOffsetProperties) other;
        return Objects.equals(topic, o.topic)
            && partition == o.partition
            && offset == o.offset;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(topic, partition, offset);
    }

    @Override
    @Generated
    public String toString() {
        return "TopicPartitionOffsetProperties{" +
            "topic='" + topic + '\'' +
            ", partition=" + partition +
            ", offset=" + offset +
            '}';
    }

}
