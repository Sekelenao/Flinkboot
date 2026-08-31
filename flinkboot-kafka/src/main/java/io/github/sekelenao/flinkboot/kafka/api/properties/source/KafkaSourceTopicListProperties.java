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

/**
 * Configuration properties for Kafka sources consuming from an explicit list of topics.
 */
public class KafkaSourceTopicListProperties implements OffsetInitializerProperties, Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<@NotBlank String> bootstrapServers;

    @NotBlank
    private final String groupId;

    @NotEmpty
    private final List<@NotBlank String> topics;

    @NotNull
    private final KafkaOffsetInitializer startingOffsets;

    @PositiveOrZero
    private final Long startingOffsetsTimestamp;

    private final List<@Valid TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code KafkaSourceTopicListProperties} instance.
     *
     * @param name                           source operator name in Flink DAG
     * @param bootstrapServers               list of Kafka broker addresses
     * @param groupId                        Kafka consumer group ID
     * @param topics                         list of topics to consume from
     * @param startingOffsets                starting offset strategy (EARLIEST, LATEST, COMMITTED, TIMESTAMP, OFFSETS)
     * @param startingOffsetsTimestamp       timestamp in milliseconds (required if startingOffsets is TIMESTAMP)
     * @param startingOffsetsPartitionOffsets list of partition offsets (required if startingOffsets is OFFSETS)
     * @param properties                     additional Kafka consumer client properties
     * @throws InvalidKafkaSourcePropertiesException if offset parameters conflict or are missing
     */
    @JsonCreator
    public KafkaSourceTopicListProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("group-id") String groupId,
        @JsonProperty("topics") List<String> topics,
        @JsonProperty("starting-offsets") KafkaOffsetInitializer startingOffsets,
        @JsonProperty("starting-offsets-timestamp") Long startingOffsetsTimestamp,
        @JsonProperty("starting-offsets-partition-offsets") List<TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topics = topics;
        this.startingOffsets = startingOffsets;
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
        return Collections.unmodifiableList(bootstrapServers);
    }

    /**
     * Returns the Kafka consumer group ID.
     *
     * @return the group ID string
     */
    public String groupId() {
        return groupId;
    }

    /**
     * Returns the list of topics to subscribe to.
     *
     * @return an unmodifiable list of topic names
     */
    public List<String> topics() {
        return Collections.unmodifiableList(topics);
    }

    /**
     * Returns the starting offset strategy.
     *
     * @return the {@link KafkaOffsetInitializer}
     */
    public KafkaOffsetInitializer startingOffsets() {
        return startingOffsets;
    }

    /**
     * Returns the optional starting offset timestamp in milliseconds.
     *
     * @return an {@link OptionalLong} containing the timestamp, or empty if not specified
     */
    public OptionalLong startingOffsetsTimestamp() {
        if (startingOffsetsTimestamp == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(startingOffsetsTimestamp);
    }

    /**
     * Returns the list of specific partition starting offsets.
     *
     * @return an unmodifiable list of {@link TopicPartitionOffsetProperties}
     */
    public List<TopicPartitionOffsetProperties> startingOffsetsPartitionOffsets() {
        if (startingOffsetsPartitionOffsets == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(startingOffsetsPartitionOffsets);
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
        if (!(other instanceof KafkaSourceTopicListProperties)) {
            return false;
        }
        var o = (KafkaSourceTopicListProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(groupId, o.groupId)
            && Objects.equals(topics, o.topics)
            && startingOffsets == o.startingOffsets
            && Objects.equals(startingOffsetsTimestamp, o.startingOffsetsTimestamp)
            && Objects.equals(startingOffsetsPartitionOffsets, o.startingOffsetsPartitionOffsets)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, groupId, topics, startingOffsets, startingOffsetsTimestamp, startingOffsetsPartitionOffsets, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaSourceTopicListProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", groupId='" + groupId + '\'' +
            ", topics=" + topics +
            ", startingOffsets=" + startingOffsets +
            ", startingOffsetsTimestamp=" + startingOffsetsTimestamp +
            ", startingOffsetsPartitionOffsets=" + startingOffsetsPartitionOffsets +
            ", properties=" + properties +
            '}';
    }
}
