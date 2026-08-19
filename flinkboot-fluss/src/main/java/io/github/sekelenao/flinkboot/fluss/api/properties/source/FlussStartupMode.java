package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import org.apache.fluss.client.initializer.OffsetsInitializer;

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
     * @return the offsets initializer, or {@code null} if mode requires a timestamp
     */
    public OffsetsInitializer offsetsInitializer() {
        return offsetsInitializer;
    }

    /**
     * Creates an {@link OffsetsInitializer} resolved for a specific timestamp in milliseconds.
     *
     * @param timestamp the timestamp in milliseconds
     * @return the resolved {@link OffsetsInitializer}
     */
    public static OffsetsInitializer fromTimestamp(long timestamp) {
        return OffsetsInitializer.timestamp(timestamp);
    }
}
