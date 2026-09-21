package io.github.sekelenao.flinkboot.test.internal.sink;

import org.apache.flink.api.connector.sink2.SinkWriter;

import java.util.Objects;
import java.util.UUID;

/**
 * Internal sink writer implementation delegating writes to CollectingSinkRegistry.
 *
 * @param <T> the type of elements written by this sink
 */
public final class CollectingSinkWriter<T> implements SinkWriter<T> {

    private final UUID sinkId;

    /**
     * Creates a collecting sink writer.
     *
     * @param sinkId the identifier of the sink
     */
    public CollectingSinkWriter(UUID sinkId) {
        this.sinkId = Objects.requireNonNull(sinkId, "sinkId must not be null");
    }

    /**
     * Writes an element to the collecting sink registry.
     *
     * @param element the element to write
     * @param context the context for the write operation
     */
    @Override
    public void write(T element, Context context) {
        CollectingSinkRegistry.append(sinkId, element);
    }

    /**
     * Flushes any buffered data.
     *
     * @param endOfInput whether the input has ended
     */
    @Override
    public void flush(boolean endOfInput) {
        // No buffering required
    }

    /**
     * Closes the sink writer.
     */
    @Override
    public void close() {
        // Lifecycle managed by CollectingSink
    }
}
