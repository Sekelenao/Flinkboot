package com.flinkboot.test.api.sink;

import com.flinkboot.test.internal.sink.CollectingSinkRegistry;
import com.flinkboot.test.internal.sink.CollectingSinkWriter;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;

import java.util.List;
import java.util.UUID;

/**
 * A thread-safe sink for collecting elements in unit and integration tests.
 *
 * <p>This sink uses a static in-memory registry in the JVM running its sink writers. It is
 * intended for an in-process MiniCluster: values written by task managers in other JVMs,
 * including a remote cluster, are not available through this sink's {@link #elements()} method.
 *
 * <p>The writer stores element references without copying. If Flink object reuse is enabled,
 * later records may mutate values that were already collected. Disable object reuse when a test
 * needs to assert collected values, or emit immutable or snapshotted values.
 *
 * @param <T> the type of elements collected by this sink
 */
public final class CollectingSink<T> implements Sink<T>, AutoCloseable {

    private static final long serialVersionUID = 1L;

    private final UUID sinkId = UUID.randomUUID();

    /**
     * Creates a sink writer using the legacy initialization context.
     *
     * @param context the legacy initialization context
     * @return a collecting sink writer
     * @deprecated since 0.5.0-1.20, for removal. Use {@link #createWriter(WriterInitContext)} instead.
     */
    @Override
    @Deprecated(since = "0.5.0-1.20", forRemoval = true)
    public SinkWriter<T> createWriter(InitContext context) {
        return new CollectingSinkWriter<>(sinkId);
    }

    /**
     * Creates a sink writer using the modern writer initialization context.
     *
     * @param context the writer initialization context
     * @return a collecting sink writer
     */
    @Override
    public SinkWriter<T> createWriter(WriterInitContext context) {
        return new CollectingSinkWriter<>(sinkId);
    }

    /**
     * Returns an immutable snapshot of the collected elements.
     *
     * @return the collected elements
     */
    public List<T> elements() {
        return CollectingSinkRegistry.elements(sinkId);
    }

    /**
     * Returns the number of elements collected by this sink.
     *
     * @return the number of collected elements
     */
    public int size() {
        return CollectingSinkRegistry.size(sinkId);
    }

    /**
     * Returns {@code true} if this sink contains no elements.
     *
     * @return {@code true} if this sink contains no elements
     */
    public boolean isEmpty() {
        return CollectingSinkRegistry.isEmpty(sinkId);
    }

    /**
     * Removes all collected elements for this sink.
     */
    public void clear() {
        CollectingSinkRegistry.clear(sinkId);
    }

    /**
     * Closes this sink and removes all collected elements.
     */
    @Override
    public void close() {
        clear();
    }

    /**
     * Returns a string representation of this collecting sink displaying its collected elements.
     *
     * @return a string representation of this sink
     */
    @Override
    public String toString() {
        return "CollectingSink[elements=" + elements() + "]";
    }
}
