package com.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkboot.core.api.validation.ValidatableProperties;
import com.flinkboot.core.internal.annotation.Generated;
import com.flinkboot.kafka.internal.validation.properties.KafkaSourcePropertiesValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Unified configuration properties for Apache Flink Kafka sources consuming from an explicit list of topics,
 * dynamic topics matching a regex pattern, or explicit topic partitions.
 */
public final class KafkaSourceProperties implements Serializable, ValidatableProperties {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<@NotBlank String> bootstrapServers;

    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String groupId;

    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String clientIdPrefix;

    private final List<@NotBlank String> topics;

    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String topicPattern;

    private final List<@NotNull @Valid TopicPartitionProperties> partitions;

    @Valid
    @NotNull
    private final KafkaOffsetProperties startingOffsets;

    private final KafkaBoundedness boundedness;

    @Valid
    private final KafkaOffsetProperties stoppingOffsets;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code KafkaSourceProperties} instance.
     *
     * @param name             source operator name in Flink DAG
     * @param bootstrapServers list of Kafka broker addresses
     * @param groupId          optional Kafka consumer group ID
     * @param clientIdPrefix   optional prefix for unique Kafka client IDs across source subtasks
     * @param topics           list of topics to consume from (mutually exclusive with {@code topicPattern} and {@code partitions})
     * @param topicPattern     regular expression pattern to match topics against (mutually exclusive with {@code topics} and {@code partitions})
     * @param partitions       list of specific topic partitions to consume from (mutually exclusive with {@code topics} and {@code topicPattern})
     * @param startingOffsets  starting offset configuration
     * @param boundedness      execution boundedness mode (optional, defaults to UNBOUNDED if null)
     * @param stoppingOffsets  stopping offset configuration (optional)
     * @param properties       additional Kafka consumer client properties
     */
    @JsonCreator
    public KafkaSourceProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("client-id-prefix") String clientIdPrefix,
        @JsonProperty("topics") List<String> topics,
        @JsonProperty("topic-pattern") String topicPattern,
        @JsonProperty("partitions") List<TopicPartitionProperties> partitions,
        @JsonProperty("starting-offsets") KafkaOffsetProperties startingOffsets,
        @JsonProperty("boundedness") KafkaBoundedness boundedness,
        @JsonProperty("stopping-offsets") KafkaOffsetProperties stoppingOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.clientIdPrefix = clientIdPrefix;
        this.topics = topics;
        this.topicPattern = topicPattern;
        this.partitions = partitions;
        this.startingOffsets = startingOffsets;
        this.boundedness = boundedness;
        this.stoppingOffsets = stoppingOffsets;
        this.properties = properties;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return KafkaSourcePropertiesValidator.validate(this, context);
    }

    /**
     * Returns the Flink source operator name.
     *
     * @return the source name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the list of Kafka bootstrap servers.
     *
     * @return an unmodifiable list of bootstrap server addresses
     */
    public List<String> bootstrapServers() {
        if (bootstrapServers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bootstrapServers);
    }

    /**
     * Returns the optional Kafka consumer group ID.
     *
     * @return an {@link Optional} containing the group ID, or empty if not configured
     */
    public Optional<String> groupId() {
        return Optional.ofNullable(groupId);
    }

    /**
     * Returns the optional prefix used to generate unique Kafka client IDs for source subtasks.
     *
     * @return an {@link Optional} containing the client ID prefix, or empty if not configured
     */
    public Optional<String> clientIdPrefix() {
        return Optional.ofNullable(clientIdPrefix);
    }

    /**
     * Returns the list of topics to subscribe to.
     *
     * @return an unmodifiable list of topic names
     */
    public List<String> topics() {
        if (topics == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(topics);
    }

    /**
     * Returns the regular expression topic pattern if configured.
     *
     * @return an {@link Optional} containing the topic pattern regex string, or empty if not set
     */
    public Optional<String> topicPattern() {
        return Optional.ofNullable(topicPattern);
    }

    /**
     * Returns the list of specific topic partitions to subscribe to.
     *
     * @return an unmodifiable list of topic partition configurations
     */
    public List<TopicPartitionProperties> partitions() {
        if (partitions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(partitions);
    }

    /**
     * Returns the starting offset configuration.
     *
     * @return the {@link KafkaOffsetProperties}
     */
    public KafkaOffsetProperties startingOffsets() {
        return startingOffsets;
    }

    /**
     * Returns the optional execution boundedness mode.
     *
     * @return an {@link Optional} containing the {@link KafkaBoundedness}, or empty if not configured
     */
    public Optional<KafkaBoundedness> boundedness() {
        return Optional.ofNullable(boundedness);
    }

    /**
     * Returns the optional stopping offset configuration.
     *
     * @return an {@link Optional} containing the stopping {@link KafkaOffsetProperties}, or empty if not configured
     */
    public Optional<KafkaOffsetProperties> stoppingOffsets() {
        return Optional.ofNullable(stoppingOffsets);
    }

    /**
     * Returns additional Kafka consumer client properties.
     *
     * @return an unmodifiable map of configuration properties
     */
    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof KafkaSourceProperties)) {
            return false;
        }
        var o = (KafkaSourceProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(groupId, o.groupId)
            && Objects.equals(clientIdPrefix, o.clientIdPrefix)
            && Objects.equals(topics, o.topics)
            && Objects.equals(topicPattern, o.topicPattern)
            && Objects.equals(partitions, o.partitions)
            && Objects.equals(startingOffsets, o.startingOffsets)
            && boundedness == o.boundedness
            && Objects.equals(stoppingOffsets, o.stoppingOffsets)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, groupId, clientIdPrefix, topics, topicPattern, partitions, startingOffsets, boundedness, stoppingOffsets, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaSourceProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", groupId='" + groupId + '\'' +
            ", clientIdPrefix='" + clientIdPrefix + '\'' +
            ", topics=" + topics +
            ", topicPattern='" + topicPattern + '\'' +
            ", partitions=" + partitions +
            ", startingOffsets=" + startingOffsets +
            ", boundedness=" + boundedness +
            ", stoppingOffsets=" + stoppingOffsets +
            ", properties=" + properties +
            '}';
    }
}
