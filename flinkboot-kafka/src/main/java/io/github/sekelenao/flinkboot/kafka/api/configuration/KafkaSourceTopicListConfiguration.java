package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

public class KafkaSourceTopicListConfiguration implements OffsetInitializerConfiguration {

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotEmpty
    private final List<String> topics;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    private final Long startingOffsetsTimestamp;

    private final List<@Valid TopicPartitionConfiguration> startingOffsetsPartitionOffsets;

    private final Map<String, String> properties;

    @JsonCreator
    public KafkaSourceTopicListConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topics") List<String> topics,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("starting-offsets-timestamp") Long startingOffsetsTimestamp,
        @JsonProperty("starting-offsets-partition-offsets") List<TopicPartitionConfiguration> startingOffsetsPartitionOffsets,
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
        return Collections.unmodifiableList(bootstrapServers);
    }

    public String groupId() {
        return groupId;
    }

    public List<String> topics() {
        return Collections.unmodifiableList(topics);
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

    public List<TopicPartitionConfiguration> startingOffsetsPartitionOffsets() {
        if (startingOffsetsPartitionOffsets == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(startingOffsetsPartitionOffsets);
    }

    public Optional<Map<String, String>> properties() {
        if (properties == null) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(properties));
    }
}
