package io.github.sekelenao.flinkboot.test.internal.sink;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CollectingSinkWriter")
class CollectingSinkWriterTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create writer when arguments are valid")
        void shouldCreateWriterWhenArgumentsAreValid() {
            try (var writer = new CollectingSinkWriter<String>(UUID.randomUUID())) {
                assertNotNull(writer);
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when sinkId is null")
        void shouldThrowExceptionWhenSinkIdIsNull() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> new CollectingSinkWriter<String>(null)
            );
            assertEquals("sinkId must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Write")
    class Write {

        @Test
        @DisplayName("Should write element into collecting sink registry")
        void shouldWriteElementIntoRegistry() {
            var sinkId = UUID.randomUUID();
            try (var writer = new CollectingSinkWriter<String>(sinkId)) {
                writer.write("item-1", null);
                writer.write("item-2", null);

                assertEquals(List.of("item-1", "item-2"), CollectingSinkRegistry.elements(sinkId));
            } finally {
                CollectingSinkRegistry.clear(sinkId);
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when writing null element")
        void shouldThrowExceptionWhenWritingNullElement() {
            var sinkId = UUID.randomUUID();
            try (var writer = new CollectingSinkWriter<String>(sinkId)) {
                var exception = assertThrows(
                    NullPointerException.class,
                    () -> writer.write(null, null)
                );
                assertEquals("value must not be null", exception.getMessage());
            } finally {
                CollectingSinkRegistry.clear(sinkId);
            }
        }
    }

    @Nested
    @DisplayName("Flush and close lifecycle")
    class FlushAndCloseLifecycle {

        @Test
        @DisplayName("Should execute flush without error")
        void shouldExecuteFlushWithoutError() {
            try (var writer = new CollectingSinkWriter<String>(UUID.randomUUID())) {
                assertAll(
                    () -> assertDoesNotThrow(() -> writer.flush(false)),
                    () -> assertDoesNotThrow(() -> writer.flush(true))
                );
            }
        }

        @Test
        @DisplayName("Should execute close without error")
        void shouldExecuteCloseWithoutError() {
            var writer = new CollectingSinkWriter<String>(UUID.randomUUID());

            assertDoesNotThrow(writer::close);
        }
    }
}
