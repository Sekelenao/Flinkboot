package io.github.sekelenao.flinkboot.kafka.api.configuration;

import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaOffsetInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("KafkaOffsetInitializer")
class KafkaOffsetInitializerTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class OffsetsInitializerResolution {

        @Test
        @DisplayName("Should return non-null offsets initializer for each enum value")
        void shouldReturnNonNullOffsetsInitializer() {
            assertAll(
                () -> assertNotNull(KafkaOffsetInitializer.EARLIEST.offsetsInitializer()),
                () -> assertNotNull(KafkaOffsetInitializer.LATEST.offsetsInitializer()),
                () -> assertNotNull(KafkaOffsetInitializer.COMMITTED.offsetsInitializer()),
                () -> assertNotNull(KafkaOffsetInitializer.COMMITTED_EARLIEST.offsetsInitializer()),
                () -> assertNotNull(KafkaOffsetInitializer.COMMITTED_LATEST.offsetsInitializer())
            );
        }

        @Test
        @DisplayName("Should throw UnsupportedOperationException for TIMESTAMP and OFFSETS")
        void shouldThrowExceptionForParameterizedInitializers() {
            assertAll(
                () -> assertThrows(UnsupportedOperationException.class, KafkaOffsetInitializer.TIMESTAMP::offsetsInitializer),
                () -> assertThrows(UnsupportedOperationException.class, KafkaOffsetInitializer.OFFSETS::offsetsInitializer)
            );
        }
    }
}
