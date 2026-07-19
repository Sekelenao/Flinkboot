package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public class KafkaSourceTopicPatternConfiguration implements OffsetInitializerConfiguration, Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotNull
    private final Pattern topicPattern;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    private final Long startingOffsetsTimestamp;

    private final List<@Valid TopicPartitionOffsetConfiguration> startingOffsetsPartitionOffsets;

    private final Map<String, String> properties;

    @JsonCreator
    public KafkaSourceTopicPatternConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topic-pattern") Pattern topicPattern,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("starting-offsets-timestamp") Long startingOffsetsTimestamp,
        @JsonProperty("starting-offsets-partition-offsets") List<TopicPartitionOffsetConfiguration> startingOffsetsPartitionOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topicPattern = topicPattern;
        this.startingOffsets = startingOffsets;
        this.startingOffsetsTimestamp = startingOffsetsTimestamp;
        this.startingOffsetsPartitionOffsets = startingOffsetsPartitionOffsets;
        this.properties = properties;
    }

    public List<String> bootstrapServers() {
        return Collections.unmodifiableList(bootstrapServers);
    }

    public String groupId() {
        return groupId;
    }

    public Pattern topicPattern() {
        return topicPattern;
    }

    public KafkaOffsetInitializer startingOffsets() {
        return startingOffsets;
    }

    public OptionalLong startingOffsetsTimestamp() {
        if (startingOffsetsTimestamp == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(startingOffsetsTimestamp);
    }

    public List<TopicPartitionOffsetConfiguration> startingOffsetsPartitionOffsets() {
        if (startingOffsetsPartitionOffsets == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(startingOffsetsPartitionOffsets);
    }

    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }


    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof KafkaSourceTopicPatternConfiguration)) {
            return false;
        }
        var o = (KafkaSourceTopicPatternConfiguration) other;
        return Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(groupId, o.groupId)
            && Objects.equals(topicPattern, o.topicPattern)
            && startingOffsets == o.startingOffsets
            && Objects.equals(startingOffsetsTimestamp, o.startingOffsetsTimestamp)
            && Objects.equals(startingOffsetsPartitionOffsets, o.startingOffsetsPartitionOffsets)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(bootstrapServers, groupId, topicPattern, startingOffsets, startingOffsetsTimestamp, startingOffsetsPartitionOffsets, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaSourceTopicPatternConfiguration{" +
            "bootstrapServers=" + bootstrapServers +
            ", groupId='" + groupId + '\'' +
            ", topicPattern=" + topicPattern +
            ", startingOffsets=" + startingOffsets +
            ", startingOffsetsTimestamp=" + startingOffsetsTimestamp +
            ", startingOffsetsPartitionOffsets=" + startingOffsetsPartitionOffsets +
            ", properties=" + properties +
            '}';
    }
}
