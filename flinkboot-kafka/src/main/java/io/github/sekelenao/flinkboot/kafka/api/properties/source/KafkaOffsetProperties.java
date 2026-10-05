package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import io.github.sekelenao.flinkboot.kafka.internal.validation.properties.KafkaOffsetPropertiesValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Configuration properties for Kafka offset initialization (starting or stopping offsets).
 */
public final class KafkaOffsetProperties implements Serializable, ValidatableProperties {

    private static final long serialVersionUID = 1L;

    @NotNull
    private final KafkaOffsetInitializer strategy;

    @PositiveOrZero
    private final Long timestamp;

    private final List<@NotNull @Valid TopicPartitionOffsetProperties> partitions;

    /**
     * Creates a new {@code KafkaOffsetProperties} instance.
     *
     * @param strategy   offset strategy (EARLIEST, LATEST, COMMITTED, TIMESTAMP, OFFSETS, etc.)
     * @param timestamp  timestamp in milliseconds (required if strategy is TIMESTAMP)
     * @param partitions partition offsets (required if strategy is OFFSETS)
     */
    @JsonCreator
    public KafkaOffsetProperties(
        @JsonProperty("strategy") KafkaOffsetInitializer strategy,
        @JsonProperty("timestamp") Long timestamp,
        @JsonProperty("partitions") List<TopicPartitionOffsetProperties> partitions
    ) {
        this.strategy = strategy;
        this.timestamp = timestamp;
        this.partitions = partitions;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return KafkaOffsetPropertiesValidator.validate(this, context);
    }

    /**
     * Returns the offset initializer strategy.
     *
     * @return the {@link KafkaOffsetInitializer}
     */
    public KafkaOffsetInitializer strategy() {
        return strategy;
    }

    /**
     * Returns the optional offset timestamp in milliseconds.
     *
     * @return an {@link OptionalLong} containing the timestamp, or empty if not specified
     */
    public OptionalLong timestamp() {
        if (timestamp == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(timestamp);
    }

    /**
     * Returns the list of specific topic partition offsets.
     *
     * @return an unmodifiable list of {@link TopicPartitionOffsetProperties}
     */
    public List<TopicPartitionOffsetProperties> partitions() {
        if (partitions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(partitions);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof KafkaOffsetProperties)) {
            return false;
        }
        var o = (KafkaOffsetProperties) other;
        return strategy == o.strategy
            && Objects.equals(timestamp, o.timestamp)
            && Objects.equals(partitions, o.partitions);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(strategy, timestamp, partitions);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaOffsetProperties{" +
            "strategy=" + strategy +
            ", timestamp=" + timestamp +
            ", partitions=" + partitions +
            '}';
    }
}
