package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("KafkaOffsetInitializer")
class KafkaOffsetInitializerTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class OffsetsInitializerResolution {

        @Test
        @DisplayName("Should return present optional for each parameterless initializer")
        void shouldReturnPresentOptionalForParameterlessInitializers() {
            assertAll(
                () -> assertTrue(KafkaOffsetInitializer.EARLIEST.offsetsInitializer().isPresent()),
                () -> assertTrue(KafkaOffsetInitializer.LATEST.offsetsInitializer().isPresent()),
                () -> assertTrue(KafkaOffsetInitializer.COMMITTED.offsetsInitializer().isPresent()),
                () -> assertTrue(KafkaOffsetInitializer.COMMITTED_EARLIEST.offsetsInitializer().isPresent()),
                () -> assertTrue(KafkaOffsetInitializer.COMMITTED_LATEST.offsetsInitializer().isPresent())
            );
        }

        @Test
        @DisplayName("Should return empty optional for parameterized initializers")
        void shouldReturnEmptyOptionalForParameterizedInitializers() {
            assertAll(
                () -> assertTrue(KafkaOffsetInitializer.TIMESTAMP.offsetsInitializer().isEmpty()),
                () -> assertTrue(KafkaOffsetInitializer.OFFSETS.offsetsInitializer().isEmpty())
            );
        }
    }
}
