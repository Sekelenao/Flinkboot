package io.github.sekelenao.flinkboot.test.api;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CollectingSink")
@SuppressWarnings("deprecation")
class CollectingSinkTest {

    @Nested
    @DisplayName("Creation and collection")
    class CreationAndCollection {

        @Test
        @DisplayName("Should create a sink and collect invoked elements")
        void shouldCreateSinkAndCollectInvokedElements() throws Exception {
            var sink = CollectingSink.<String>create();

            sink.invoke("event-1");
            sink.invoke("event-2");

            assertAll(
                () -> assertNotNull(sink),
                () -> assertEquals(List.of("event-1", "event-2"), sink.elements())
            );
        }

        @Test
        @DisplayName("Should collect elements from a Flink stream")
        void shouldCollectElementsFromFlinkStream() throws Exception {
            var env = StreamExecutionEnvironment.getExecutionEnvironment();
            var sink = CollectingSink.<String>create();

            env.fromElements("event-1", "event-2")
                    .addSink(sink)
                    .setParallelism(2);
            env.execute();

            var elements = sink.elements();

            assertAll(
                () -> assertEquals(2, elements.size()),
                () -> assertTrue(elements.containsAll(List.of("event-1", "event-2")))
            );
        }
    }

    @Nested
    @DisplayName("Elements")
    class Elements {

        @Test
        @DisplayName("Should return an immutable snapshot of collected elements")
        void shouldReturnImmutableSnapshotOfCollectedElements() throws Exception {
            var sink = CollectingSink.<String>create();
            sink.invoke("event-1");

            var elements = sink.elements();
            sink.invoke("event-2");

            assertAll(
                () -> assertEquals(List.of("event-1"), elements),
                () -> assertThrows(UnsupportedOperationException.class, () -> elements.add("event-3")),
                () -> assertEquals(List.of("event-1", "event-2"), sink.elements())
            );
        }
    }

    @Nested
    @DisplayName("Clear")
    class Clear {

        @Test
        @DisplayName("Should remove all collected elements")
        void shouldRemoveAllCollectedElements() throws Exception {
            var sink = CollectingSink.<String>create();
            sink.invoke("event-1");
            sink.invoke("event-2");

            sink.clear();

            assertTrue(sink.elements().isEmpty());
        }
    }

    @Nested
    @DisplayName("Concurrent access")
    class ConcurrentAccess {

        @Test
        @DisplayName("Should collect elements invoked concurrently")
        void shouldCollectElementsInvokedConcurrently() throws Exception {
            var sink = CollectingSink.<Integer>create();
            var workerCount = 4;
            var elementsPerWorker = 100;
            var start = new CountDownLatch(1);
            var finished = new CountDownLatch(workerCount);
            var executor = Executors.newFixedThreadPool(workerCount);

            try {
                for (var worker = 0; worker < workerCount; worker++) {
                    var workerId = worker;
                    executor.submit(() -> {
                        try {
                            start.await();
                            for (var element = 0; element < elementsPerWorker; element++) {
                                sink.invoke(workerId * elementsPerWorker + element);
                            }
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                        } finally {
                            finished.countDown();
                        }
                    });
                }

                start.countDown();

                assertTrue(finished.await(5, TimeUnit.SECONDS));
                assertEquals(workerCount * elementsPerWorker, sink.elements().size());
            } finally {
                executor.shutdownNow();
            }
        }
    }
}
