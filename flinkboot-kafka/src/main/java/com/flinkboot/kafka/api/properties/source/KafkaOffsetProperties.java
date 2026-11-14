package com.flinkboot.kafka.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkboot.core.api.validation.ValidatableProperties;
import com.flinkboot.core.internal.annotation.Generated;
import com.flinkboot.kafka.internal.validation.properties.offset.KafkaOffsetPropertiesValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    private final KafkaOffsetResetStrategy resetStrategy;

    /**
     * Creates a new {@code KafkaOffsetProperties} instance.
     *
     * @param strategy      offset strategy (EARLIEST, LATEST, COMMITTED, TIMESTAMP, OFFSETS, etc.)
     * @param timestamp     timestamp in milliseconds (required if strategy is TIMESTAMP)
     * @param partitions    partition offsets (required if strategy is OFFSETS)
     * @param resetStrategy reset strategy for unassigned partitions (required if strategy is OFFSETS)
     */
    @JsonCreator
    public KafkaOffsetProperties(
        @JsonProperty("strategy") KafkaOffsetInitializer strategy,
        @JsonProperty("timestamp") Long timestamp,
        @JsonProperty("partitions") List<TopicPartitionOffsetProperties> partitions,
        @JsonProperty("reset-strategy") KafkaOffsetResetStrategy resetStrategy
    ) {
        this.strategy = strategy;
        this.timestamp = timestamp;
        this.partitions = partitions;
        this.resetStrategy = resetStrategy;
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

    /**
     * Returns the optional reset strategy for partitions without explicit offsets.
     *
     * @return an {@link Optional} containing the reset strategy, or empty if not specified
     */
    public Optional<KafkaOffsetResetStrategy> resetStrategy() {
        return Optional.ofNullable(resetStrategy);
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
            && Objects.equals(partitions, o.partitions)
            && resetStrategy == o.resetStrategy;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(strategy, timestamp, partitions, resetStrategy);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaOffsetProperties{" +
            "strategy=" + strategy +
            ", timestamp=" + timestamp +
            ", partitions=" + partitions +
            ", resetStrategy=" + resetStrategy +
            '}';
    }
}
