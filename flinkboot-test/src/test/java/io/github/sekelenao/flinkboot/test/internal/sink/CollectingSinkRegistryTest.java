package io.github.sekelenao.flinkboot.test.internal.sink;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CollectingSinkRegistry")
class CollectingSinkRegistryTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when instantiated via reflection")
        void shouldThrowAssertionErrorWhenInstantiatedViaReflection() throws Exception {
            var constructor = CollectingSinkRegistry.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

            assertInstanceOf(AssertionError.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Null contract")
    class NullContract {

        @Test
        @DisplayName("Should throw NullPointerException when appending with null sinkId")
        void shouldThrowExceptionWhenAppendingWithNullSinkId() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> CollectingSinkRegistry.append(null, "value")
            );
            assertEquals("sinkId must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when appending with null value")
        void shouldThrowExceptionWhenAppendingWithNullValue() {
            var sinkId = UUID.randomUUID();
            var exception = assertThrows(
                NullPointerException.class,
                () -> CollectingSinkRegistry.append(sinkId, null)
            );
            assertEquals("value must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when retrieving elements with null sinkId")
        void shouldThrowExceptionWhenRetrievingWithNullSinkId() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> CollectingSinkRegistry.elements(null)
            );
            assertEquals("sinkId must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when clearing with null sinkId")
        void shouldThrowExceptionWhenClearingWithNullSinkId() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> CollectingSinkRegistry.clear(null)
            );
            assertEquals("sinkId must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Storage and retrieval")
    class StorageAndRetrieval {

        @Test
        @DisplayName("Should return empty list when sink identifier has no registered elements")
        void shouldReturnEmptyListForUnknownSinkId() {
            var elements = CollectingSinkRegistry.elements(UUID.randomUUID());

            assertAll(
                () -> assertNotNull(elements),
                () -> assertTrue(elements.isEmpty())
            );
        }

        @Test
        @DisplayName("Should append and retrieve elements in order")
        void shouldAppendAndRetrieveElements() {
            var sinkId = UUID.randomUUID();

            CollectingSinkRegistry.append(sinkId, "first");
            CollectingSinkRegistry.append(sinkId, "second");

            assertEquals(List.of("first", "second"), CollectingSinkRegistry.elements(sinkId));
        }

        @Test
        @DisplayName("Should return an immutable snapshot of elements")
        void shouldReturnImmutableSnapshot() {
            var sinkId = UUID.randomUUID();
            CollectingSinkRegistry.append(sinkId, "first");

            List<String> snapshot = CollectingSinkRegistry.elements(sinkId);

            assertThrows(UnsupportedOperationException.class, () -> snapshot.add("mutated"));
        }

        @Test
        @DisplayName("Should remove elements when cleared")
        void shouldRemoveElementsWhenCleared() {
            var sinkId = UUID.randomUUID();
            CollectingSinkRegistry.append(sinkId, "item");

            CollectingSinkRegistry.clear(sinkId);

            assertTrue(CollectingSinkRegistry.elements(sinkId).isEmpty());
        }

        @Test
        @DisplayName("Should safely allow idempotent clear calls")
        void shouldAllowIdempotentClear() {
            var sinkId = UUID.randomUUID();

            assertDoesNotThrow(() -> {
                CollectingSinkRegistry.clear(sinkId);
                CollectingSinkRegistry.clear(sinkId);
            });
        }
    }
}
