package io.github.sekelenao.flinkboot.kafka.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class KafkaSourceTopicPatternConfiguration {

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotNull
    private final Pattern topicPattern;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    private final Map<String, String> properties;

    @JsonCreator
    public KafkaSourceTopicPatternConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topic-pattern") Pattern topicPattern,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topicPattern = topicPattern;
        this.startingOffsets = startingOffsets;
        this.properties = properties;
    }

    public List<String> bootstrapServers() {
        return bootstrapServers;
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

    public Optional<Map<String, String>> properties() {
        return Optional.ofNullable(properties);
    }
}
