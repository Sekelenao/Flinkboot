package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;

public final class TopicPartitionConfiguration {

    @NotBlank
    private final String topic;

    @PositiveOrZero
    private final int partition;

    @PositiveOrZero
    private final long offset;

    @JsonCreator
    public TopicPartitionConfiguration(
        @JsonProperty("topic") String topic,
        @JsonProperty("partition") int partition,
        @JsonProperty("offset") long offset
    ) {
        this.topic = topic;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicPartitionConfiguration that = (TopicPartitionConfiguration) o;
        return partition == that.partition && offset == that.offset && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, partition, offset);
    }

    @Override
    public String toString() {
        return topic + "-" + partition + ":" + offset;
    }
}
