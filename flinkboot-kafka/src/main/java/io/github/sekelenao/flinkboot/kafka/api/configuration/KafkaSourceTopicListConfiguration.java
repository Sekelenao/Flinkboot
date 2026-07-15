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

    private final Map<String, String> properties;

    @JsonCreator
    public KafkaSourceTopicListConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topics") List<String> topics,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topics = topics;
        this.startingOffsets = startingOffsets;
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

    public Optional<Map<String, String>> properties() {
        return Optional.ofNullable(properties);
    }
}
