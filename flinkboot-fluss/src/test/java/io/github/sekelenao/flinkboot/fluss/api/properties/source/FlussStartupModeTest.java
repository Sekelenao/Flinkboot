package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("FlussStartupMode")
class FlussStartupModeTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class Resolution {

        @Test
        @DisplayName("Should provide non-null OffsetsInitializer for static modes")
        void shouldProvideOffsetsInitializerForStaticModes() {
            assertAll(
                () -> assertNotNull(FlussStartupMode.EARLIEST.offsetsInitializer()),
                () -> assertNotNull(FlussStartupMode.LATEST.offsetsInitializer()),
                () -> assertNotNull(FlussStartupMode.FULL.offsetsInitializer()),
                () -> assertNull(FlussStartupMode.TIMESTAMP.offsetsInitializer())
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
