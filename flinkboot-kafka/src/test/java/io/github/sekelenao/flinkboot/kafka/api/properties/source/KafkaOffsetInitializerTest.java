package io.github.sekelenao.flinkboot.kafka.api.properties.source;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaOffsetInitializer")
class KafkaOffsetInitializerTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class OffsetsInitializerResolution {

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {
                "EARLIEST", "LATEST", "COMMITTED", "COMMITTED_EARLIEST", "COMMITTED_LATEST"
        })
        @DisplayName("Should return present optional for parameterless initializers")
        void shouldReturnPresentOptionalForParameterlessInitializers(KafkaOffsetInitializer initializer) {
            assertTrue(initializer.offsetsInitializer().isPresent());
        }

        @Test
        @DisplayName("Should configure correct reset strategy for static offset initializers")
        void shouldConfigureCorrectResetStrategy() {
            assertAll(
                    () -> assertEquals(OffsetResetStrategy.EARLIEST,
                            KafkaOffsetInitializer.EARLIEST.offsetsInitializer().orElseThrow()
                                    .getAutoOffsetResetStrategy()),
                    () -> assertEquals(OffsetResetStrategy.LATEST,
                            KafkaOffsetInitializer.LATEST.offsetsInitializer().orElseThrow()
                                    .getAutoOffsetResetStrategy()),
                    () -> assertEquals(OffsetResetStrategy.NONE,
                            KafkaOffsetInitializer.COMMITTED.offsetsInitializer().orElseThrow()
                                    .getAutoOffsetResetStrategy()),
                    () -> assertEquals(OffsetResetStrategy.EARLIEST,
                            KafkaOffsetInitializer.COMMITTED_EARLIEST.offsetsInitializer().orElseThrow()
                                    .getAutoOffsetResetStrategy()),
                    () -> assertEquals(OffsetResetStrategy.LATEST, KafkaOffsetInitializer.COMMITTED_LATEST
                            .offsetsInitializer().orElseThrow().getAutoOffsetResetStrategy()));
        }

        @ParameterizedTest
        @EnumSource(value = KafkaOffsetInitializer.class, names = {"TIMESTAMP", "OFFSETS"})
        @DisplayName("Should return empty optional for parameterized initializers")
        void shouldReturnEmptyOptionalForParameterizedInitializers(KafkaOffsetInitializer initializer) {
            assertTrue(initializer.offsetsInitializer().isEmpty());
        }
    }
}
