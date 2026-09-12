package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlussStartupMode")
class FlussStartupModeTest {

    @Nested
    @DisplayName("OffsetsInitializer Resolution")
    class Resolution {

        @ParameterizedTest
        @EnumSource(value = FlussStartupMode.class, names = {"EARLIEST", "LATEST", "FULL"})
        @DisplayName("Should provide non-empty OffsetsInitializer for static modes")
        void shouldProvideOffsetsInitializerForStaticModes(FlussStartupMode mode) {
            var initializer = mode.offsetsInitializer();
            assertAll(
                () -> assertTrue(initializer.isPresent()),
                () -> assertNotNull(initializer.get())
            );
        }

        @Test
        @DisplayName("Should provide empty OffsetsInitializer for TIMESTAMP mode")
        void shouldProvideEmptyOffsetsInitializerForTimestampMode() {
            assertTrue(FlussStartupMode.TIMESTAMP.offsetsInitializer().isEmpty());
        }

        @Test
        @DisplayName("Should create OffsetsInitializer from timestamp")
        void shouldCreateFromTimestamp() {
            var initializer = FlussStartupMode.fromTimestamp(1700000000000L);
            assertNotNull(initializer);
        }
    }
}
