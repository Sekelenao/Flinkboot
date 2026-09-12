package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlussStartupMode")
class FlussStartupModeTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class Resolution {

        @Test
        @DisplayName("Should provide non-empty Optional for static modes and empty for timestamp mode")
        void shouldProvideOffsetsInitializerForStaticModes() {
            assertAll(
                () -> assertTrue(FlussStartupMode.EARLIEST.offsetsInitializer().isPresent()),
                () -> assertTrue(FlussStartupMode.LATEST.offsetsInitializer().isPresent()),
                () -> assertTrue(FlussStartupMode.FULL.offsetsInitializer().isPresent()),
                () -> assertFalse(FlussStartupMode.TIMESTAMP.offsetsInitializer().isPresent())
            );
        }

        @Test
        @DisplayName("Should create OffsetsInitializer from timestamp")
        void shouldCreateFromTimestamp() {
            var initializer = FlussStartupMode.fromTimestamp(1700000000000L);
            assertNotNull(initializer);
        }
    }
}
