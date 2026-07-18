package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KafkaSourceTopicListConfiguration {

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotEmpty
    private final List<String> topics;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    private final Long startingOffsetsTimestamp;

    private final Map<String, Long> startingOffsetsPartitionOffsets;

    private final Map<String, String> properties;

    @JsonCreator
    public KafkaSourceTopicListConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topics") List<String> topics,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("starting-offsets-timestamp") Long startingOffsetsTimestamp,
        @JsonProperty("starting-offsets-partition-offsets") Map<String, Long> startingOffsetsPartitionOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topics = topics;
        this.startingOffsets = startingOffsets;
        this.startingOffsetsTimestamp = startingOffsetsTimestamp;
        this.startingOffsetsPartitionOffsets = startingOffsetsPartitionOffsets;
        this.properties = properties;
    }

    public List<String> bootstrapServers() {
        return bootstrapServers;
    }

    public String groupId() {
        return groupId;
    }

    public List<String> topics() {
        return topics;
    }

    public KafkaOffsetInitializer startingOffsets() {
        return startingOffsets;
    }

    public Optional<Long> startingOffsetsTimestamp() {
        return Optional.ofNullable(startingOffsetsTimestamp);
    }

    public Optional<Map<String, Long>> startingOffsetsPartitionOffsets() {
        return Optional.ofNullable(startingOffsetsPartitionOffsets);
    }

    public Optional<Map<String, String>> properties() {
        return Optional.ofNullable(properties);
    }
}
