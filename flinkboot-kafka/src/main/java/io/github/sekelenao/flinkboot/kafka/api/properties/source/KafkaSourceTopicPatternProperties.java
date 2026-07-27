package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import io.github.sekelenao.flinkboot.kafka.api.exception.InvalidKafkaSourcePropertiesException;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

public class KafkaSourceTopicPatternProperties implements OffsetInitializerProperties, Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotBlank
    private final String topicPattern;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    @PositiveOrZero
    private final Long startingOffsetsTimestamp;

    private final List<@Valid TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets;

    private final Map<@NotNull String, @NotNull String> properties;

    @JsonCreator
    public KafkaSourceTopicPatternProperties(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topic-pattern") String topicPattern,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("starting-offsets-timestamp") Long startingOffsetsTimestamp,
        @JsonProperty("starting-offsets-partition-offsets") List<TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.groupId = Objects.requireNonNull(groupId);
        this.topicPattern = Objects.requireNonNull(topicPattern);
        this.startingOffsets = Objects.requireNonNull(startingOffsets);
        this.startingOffsetsTimestamp = startingOffsetsTimestamp;
        this.startingOffsetsPartitionOffsets = startingOffsetsPartitionOffsets;
        this.properties = properties;
        validate();
    }

    private void validate() {
        if (startingOffsets == KafkaOffsetInitializer.TIMESTAMP) {
            if (startingOffsetsTimestamp == null) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-timestamp is required when starting-offsets is TIMESTAMP");
            }
            if (startingOffsetsPartitionOffsets != null && !startingOffsetsPartitionOffsets.isEmpty()) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-partition-offsets must not be specified when starting-offsets is TIMESTAMP");
            }
        } else if (startingOffsets == KafkaOffsetInitializer.OFFSETS) {
            if (startingOffsetsPartitionOffsets == null || startingOffsetsPartitionOffsets.isEmpty()) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-partition-offsets is required and cannot be empty when starting-offsets is OFFSETS");
            }
            if (startingOffsetsTimestamp != null) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-timestamp must not be specified when starting-offsets is OFFSETS");
            }
        } else if (startingOffsets != null) {
            if (startingOffsetsTimestamp != null) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-timestamp must not be specified when starting-offsets is " + startingOffsets);
            }
            if (startingOffsetsPartitionOffsets != null && !startingOffsetsPartitionOffsets.isEmpty()) {
                throw new InvalidKafkaSourcePropertiesException("starting-offsets-partition-offsets must not be specified when starting-offsets is " + startingOffsets);
            }
        }
    }

    public List<String> bootstrapServers() {
        return Collections.unmodifiableList(bootstrapServers);
    }

    public String groupId() {
        return groupId;
    }

    public String topicPattern() {
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

    public List<TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets() {
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
        if (!(other instanceof KafkaSourceTopicPatternProperties)) {
            return false;
        }
        var o = (KafkaSourceTopicPatternProperties) other;
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
        return "KafkaSourceTopicPatternProperties{" +
            "bootstrapServers=" + bootstrapServers +
            ", groupId='" + groupId + '\'' +
            ", topicPattern='" + topicPattern + '\'' +
            ", startingOffsets=" + startingOffsets +
            ", startingOffsetsTimestamp=" + startingOffsetsTimestamp +
            ", startingOffsetsPartitionOffsets=" + startingOffsetsPartitionOffsets +
            ", properties=" + properties +
            '}';
    }
}
