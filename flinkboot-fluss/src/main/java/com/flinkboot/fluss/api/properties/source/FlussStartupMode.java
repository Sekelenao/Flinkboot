package com.flinkboot.fluss.api.properties.source;

import org.apache.fluss.client.initializer.OffsetsInitializer;

import java.util.Optional;

/**
 * Startup modes for Apache Fluss sources in Apache Flink pipelines.
 */
public enum FlussStartupMode {

    /**
     * Start reading from the earliest available offset or log.
     */
    EARLIEST(OffsetsInitializer.earliest()),

    /**
     * Start reading from the latest available offset.
     */
    LATEST(OffsetsInitializer.latest()),

    /**
     * Perform a full snapshot scan followed by continuous log reading (for Primary Key tables).
     */
    FULL(OffsetsInitializer.full()),

    /**
     * Start reading from a specific timestamp in milliseconds.
     */
    TIMESTAMP(null);

    private final OffsetsInitializer offsetsInitializer;

    FlussStartupMode(OffsetsInitializer offsetsInitializer) {
        this.offsetsInitializer = offsetsInitializer;
    }

    /**
     * Returns the underlying Fluss {@link OffsetsInitializer} for static modes.
     *
     * @return an {@link Optional} containing the initializer for static modes, or empty for timestamp-based mode
     */
    public Optional<OffsetsInitializer> offsetsInitializer() {
        return Optional.ofNullable(offsetsInitializer);
    }

    /**
     * Creates an {@link OffsetsInitializer} resolved for a specific timestamp in milliseconds.
     *
     * @param timestamp the timestamp in milliseconds; must be non-negative
     * @return the resolved {@link OffsetsInitializer}
     * @throws IllegalArgumentException if the timestamp is negative
     */
    public static OffsetsInitializer fromTimestamp(long timestamp) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must be non-negative: " + timestamp);
        }
        return OffsetsInitializer.timestamp(timestamp);
    }
}
