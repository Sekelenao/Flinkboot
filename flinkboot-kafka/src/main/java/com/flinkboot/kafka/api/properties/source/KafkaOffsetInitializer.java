package com.flinkboot.kafka.api.properties.source;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.util.Optional;

/**
 * Offset initialization strategies for Kafka sources.
 */
public enum KafkaOffsetInitializer {
    /**
     * Offset boundary at the earliest available offset in each partition.
     */
    EARLIEST(OffsetsInitializer.earliest(), false),
    /**
     * Offset boundary at the latest available offset (end) in each partition.
     */
    LATEST(OffsetsInitializer.latest(), false),
    /**
     * Offset boundary at committed consumer group offsets.
     */
    COMMITTED(OffsetsInitializer.committedOffsets(), true),
    /**
     * Offset boundary at committed consumer group offsets, falling back to earliest if none are committed.
     */
    COMMITTED_EARLIEST(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST), true),
    /**
     * Offset boundary at committed consumer group offsets, falling back to latest if none are committed.
     */
    COMMITTED_LATEST(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST), true),
    /**
     * Offset boundary at a specific timestamp (requires {@code timestamp}).
     */
    TIMESTAMP(null, false),
    /**
     * Offset boundary at explicit partition offsets (requires {@code partitions}).
     */
    OFFSETS(null, false);

    private final OffsetsInitializer offsetsInitializer;

    private final boolean requiresGroupId;

    KafkaOffsetInitializer(OffsetsInitializer offsetsInitializer, boolean requiresGroupId) {
        this.offsetsInitializer = offsetsInitializer;
        this.requiresGroupId = requiresGroupId;
    }

    /**
     * Creates the corresponding Flink {@link OffsetsInitializer}.
     *
     * @return an {@link Optional} containing the corresponding initializer,
     *         or empty if the strategy requires additional parameters
     */
    public Optional<OffsetsInitializer> offsetsInitializer() {
        return Optional.ofNullable(offsetsInitializer);
    }

    /**
     * Returns whether this strategy requires a Kafka consumer group ID.
     *
     * @return {@code true} if this strategy reads committed offsets from a consumer group
     */
    public boolean requiresGroupId() {
        return requiresGroupId;
    }
}
