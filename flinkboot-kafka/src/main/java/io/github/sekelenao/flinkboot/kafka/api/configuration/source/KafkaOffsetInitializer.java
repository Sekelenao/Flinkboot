package io.github.sekelenao.flinkboot.kafka.api.configuration.source;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

public enum KafkaOffsetInitializer {
    EARLIEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.earliest();
        }
    },
    LATEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.latest();
        }
    },
    COMMITTED {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets();
        }
    },
    COMMITTED_EARLIEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST);
        }
    },
    COMMITTED_LATEST {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            return OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST);
        }
    },
    TIMESTAMP {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            throw new UnsupportedOperationException("TIMESTAMP offset initializer requires a timestamp parameter");
        }
    },
    OFFSETS {
        @Override
        public OffsetsInitializer offsetsInitializer() {
            throw new UnsupportedOperationException("OFFSETS offset initializer requires partition offsets parameters");
        }
    };

    public abstract OffsetsInitializer offsetsInitializer();
}
