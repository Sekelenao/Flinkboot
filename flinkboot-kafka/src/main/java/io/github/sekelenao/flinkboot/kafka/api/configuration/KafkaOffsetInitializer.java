package io.github.sekelenao.flinkboot.kafka.api.configuration;

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
    };

    public abstract OffsetsInitializer offsetsInitializer();
}
