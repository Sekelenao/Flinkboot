package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import java.util.Optional;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

/**
 * Offset initialization strategies for Kafka sources.
 */
public enum KafkaOffsetInitializer {
    /**
     * Start consuming from the earliest available offset in each partition.
     */
    EARLIEST(OffsetsInitializer.earliest()),
    /**
     * Start consuming from the latest available offset (end) in each partition.
     */
    LATEST(OffsetsInitializer.latest()),
    /**
     * Start consuming from committed consumer group offsets.
     */
    COMMITTED(OffsetsInitializer.committedOffsets()),
    /**
     * Start consuming from committed consumer group offsets, falling back to earliest if none are committed.
     */
    COMMITTED_EARLIEST(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)),
    /**
     * Start consuming from committed consumer group offsets, falling back to latest if none are committed.
     */
    COMMITTED_LATEST(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST)),
    /**
     * Start consuming from a specific timestamp (requires {@code starting-offsets-timestamp}).
     */
    TIMESTAMP(null),
    /**
     * Start consuming from explicit partition offsets (requires {@code starting-offsets-partition-offsets}).
     */
    OFFSETS(null);

    private final OffsetsInitializer offsetsInitializer;

    KafkaOffsetInitializer(OffsetsInitializer offsetsInitializer) {
        this.offsetsInitializer = offsetsInitializer;
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
}
