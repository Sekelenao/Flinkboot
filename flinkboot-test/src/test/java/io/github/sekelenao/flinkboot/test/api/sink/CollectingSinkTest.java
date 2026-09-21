package io.github.sekelenao.flinkboot.test.api.sink;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CollectingSink")
class CollectingSinkTest {

    @Nested
    @DisplayName("Creation and collection")
    class CreationAndCollection {

        @Test
        @DisplayName("Should create sink and return empty elements initially")
        void shouldReturnEmptyElementsInitially() {
            try (var sink = new CollectingSink<String>()) {
                assertAll(
                    () -> assertNotNull(sink),
                    () -> assertTrue(sink.elements().isEmpty())
                );
            }
        }

        @Test
        @DisplayName("Should collect elements from a parallel Flink stream using sinkTo")
        void shouldCollectElementsFromFlinkStream() throws Exception {
            var env = StreamExecutionEnvironment.getExecutionEnvironment();

            try (var sink = new CollectingSink<String>()) {
                var expected = IntStream.rangeClosed(1, 100)
                        .mapToObj(index -> "event-" + index)
                        .collect(Collectors.toList());

                env.fromData(expected)
                        .rebalance()
                        .sinkTo(sink)
                        .setParallelism(4);
                env.execute();

                var elements = sink.elements();

                assertAll(
                    () -> assertEquals(100, elements.size()),
                    () -> assertTrue(elements.containsAll(expected))
                );
            }
        }
    }

    @Nested
    @DisplayName("Writer creation")
    class WriterCreation {

        @Test
        @DisplayName("Should create writer with modern WriterInitContext")
        void shouldCreateWriterWithModernContext() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);

                assertNotNull(writer);

                writer.write("modern-event", null);

                assertEquals(List.of("modern-event"), sink.elements());
            }
        }

        @Test
        @SuppressWarnings("deprecation")
        @DisplayName("Should create writer with legacy InitContext")
        void shouldCreateWriterWithLegacyContext() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((Sink.InitContext) null);

                assertNotNull(writer);

                writer.write("legacy-event", null);

                assertEquals(List.of("legacy-event"), sink.elements());
            }
        }
    }

    @Nested
    @DisplayName("Multi-instance isolation")
    class MultiInstanceIsolation {

        @Test
        @DisplayName("Should isolate collected elements between distinct sink instances")
        void shouldIsolateElementsBetweenDistinctInstances() throws Exception {
            try (var sink1 = new CollectingSink<String>();
                 var sink2 = new CollectingSink<String>()) {

                sink1.createWriter((WriterInitContext) null).write("sink-1-event", null);
                sink2.createWriter((WriterInitContext) null).write("sink-2-event", null);

                sink1.clear();

                assertAll(
                    () -> assertTrue(sink1.elements().isEmpty()),
                    () -> assertEquals(List.of("sink-2-event"), sink2.elements())
                );
            }
        }
    }

    @Nested
    @DisplayName("Elements snapshot and immutability")
    class ElementsSnapshotAndImmutability {

        @Test
        @DisplayName("Should return an immutable snapshot of collected elements")
        void shouldReturnImmutableSnapshotOfCollectedElements() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);

                var elements = sink.elements();
                writer.write("event-2", null);

                assertAll(
                    () -> assertEquals(List.of("event-1"), elements),
                    () -> assertThrows(UnsupportedOperationException.class, () -> elements.add("event-3")),
                    () -> assertEquals(List.of("event-1", "event-2"), sink.elements())
                );
            }
        }
    }

    @Nested
    @DisplayName("Clear and close lifecycle")
    class ClearAndCloseLifecycle {

        @Test
        @DisplayName("Should remove all collected elements when cleared")
        void shouldRemoveAllCollectedElementsWhenCleared() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);
                writer.write("event-2", null);

                sink.clear();

                assertTrue(sink.elements().isEmpty());
            }
        }

        @Test
        @DisplayName("Should safely allow multiple idempotent clear calls and subsequent collection")
        void shouldSafelyAllowIdempotentClearAndSubsequentCollection() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);

                assertDoesNotThrow(sink::clear);
                assertDoesNotThrow(sink::clear);

                writer.write("event-after-clear", null);

                assertEquals(List.of("event-after-clear"), sink.elements());
            }
        }

        @Test
        @DisplayName("Should clear elements when closed")
        void shouldClearElementsWhenClosed() throws Exception {
            var sink = new CollectingSink<String>();
            var writer = sink.createWriter((WriterInitContext) null);
            writer.write("event-1", null);

            sink.close();

            assertTrue(sink.elements().isEmpty());
        }

        @Test
        @DisplayName("Should clear elements automatically when used with try-with-resources")
        void shouldClearElementsAutomaticallyWithTryWithResources() throws Exception {
            CollectingSink<String> sinkReference;
            try (var sink = new CollectingSink<String>()) {
                sinkReference = sink;
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-in-try", null);
                assertEquals(List.of("event-in-try"), sink.elements());
            }

            assertTrue(sinkReference.elements().isEmpty());
        }
    }
}
