package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

/**
 * Offset initialization strategies for Kafka sources.
 */
public enum KafkaOffsetInitializer {
    /**
     * Start consuming from the earliest available offset in each partition.
     */
    EARLIEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.earliest();
        }
    },
    /**
     * Start consuming from the latest available offset (end) in each partition.
     */
    LATEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.latest();
        }
    },
    /**
     * Start consuming from committed consumer group offsets.
     */
    COMMITTED {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets();
        }
    },
    /**
     * Start consuming from committed consumer group offsets, falling back to earliest if none are committed.
     */
    COMMITTED_EARLIEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST);
        }
    },
    /**
     * Start consuming from committed consumer group offsets, falling back to latest if none are committed.
     */
    COMMITTED_LATEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST);
        }
    },
    /**
     * Start consuming from a specific timestamp (requires {@code starting-offsets-timestamp}).
     */
    TIMESTAMP {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            throw new UnsupportedOperationException("TIMESTAMP offset initializer requires a timestamp parameter");
        }
    },
    /**
     * Start consuming from explicit partition offsets (requires {@code starting-offsets-partition-offsets}).
     */
    OFFSETS {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            throw new UnsupportedOperationException("OFFSETS offset initializer requires partition offsets parameters");
        }
    };

    /**
     * Creates the corresponding Flink {@link OffsetsInitializer}.
     *
     * @return the {@link OffsetsInitializer} instance
     * @throws UnsupportedOperationException if this strategy requires additional parameters (TIMESTAMP, OFFSETS)
     */
    public abstract OffsetsInitializer offsetsInitializer();
}
