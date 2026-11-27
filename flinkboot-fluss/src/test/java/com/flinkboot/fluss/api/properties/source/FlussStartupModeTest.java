package com.flinkboot.fluss.api.properties.source;

import org.apache.fluss.client.initializer.OffsetsInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        @Nested
        @DisplayName("fromTimestamp")
        class FromTimestamp {

            @ParameterizedTest
            @ValueSource(longs = {-1L, Long.MIN_VALUE})
            @DisplayName("Should reject negative timestamps")
            void shouldRejectNegativeTimestamps(long timestamp) {
                IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> FlussStartupMode.fromTimestamp(timestamp)
                );
                assertEquals("timestamp must be non-negative: " + timestamp, exception.getMessage());
            }

            @ParameterizedTest
            @ValueSource(longs = {0L, 1L, 1700000000000L, Long.MAX_VALUE})
            @DisplayName("Should forward non-negative timestamps to BucketOffsetsRetriever")
            void shouldForwardNonNegativeTimestamps(long timestamp) {
                OffsetsInitializer.BucketOffsetsRetriever retriever = mock(OffsetsInitializer.BucketOffsetsRetriever.class);
                when(retriever.offsetsFromTimestamp(null, Collections.emptyList(), timestamp))
                    .thenReturn(Collections.emptyMap());

                OffsetsInitializer initializer = FlussStartupMode.fromTimestamp(timestamp);
                assertNotNull(initializer);

                Map<Integer, Long> result = initializer.getBucketOffsets(
                    null,
                    Collections.emptyList(),
                    retriever
                );

                assertEquals(Collections.emptyMap(), result);
                verify(retriever).offsetsFromTimestamp(null, Collections.emptyList(), timestamp);
            }
        }
    }
}
