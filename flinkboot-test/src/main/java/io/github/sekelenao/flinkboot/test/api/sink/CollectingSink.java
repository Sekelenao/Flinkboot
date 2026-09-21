package io.github.sekelenao.flinkboot.test.api.sink;

import io.github.sekelenao.flinkboot.test.internal.sink.CollectingSinkRegistry;
import io.github.sekelenao.flinkboot.test.internal.sink.CollectingSinkWriter;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;

import java.util.List;
import java.util.UUID;

/**
 * A thread-safe sink for collecting elements in unit and integration tests.
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
}
