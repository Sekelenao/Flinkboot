package io.github.sekelenao.flinkboot.test.internal.sink;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Internal in-memory registry storing collected elements by sink identifier.
 */
public final class CollectingSinkRegistry {

    private static final Map<UUID, Queue<Object>> ELEMENTS_BY_SINK = new ConcurrentHashMap<>();

    private CollectingSinkRegistry() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Appends an element to the specified sink.
     *
     * @param sinkId the identifier of the sink
     * @param value  the element to append
     */
    public static void append(UUID sinkId, Object value) {
        Objects.requireNonNull(sinkId, "sinkId must not be null");
        Objects.requireNonNull(value, "value must not be null");
        ELEMENTS_BY_SINK.computeIfAbsent(sinkId, ignored -> new ConcurrentLinkedQueue<>())
            .add(value);
    }

    /**
     * Returns an immutable snapshot of elements collected by the specified sink.
     *
     * @param <T>    the type of elements
     * @param sinkId the identifier of the sink
     * @return an immutable snapshot of the collected elements
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> elements(UUID sinkId) {
        Objects.requireNonNull(sinkId, "sinkId must not be null");
        var queue = ELEMENTS_BY_SINK.get(sinkId);
        if (queue == null) {
            return Collections.emptyList();
        }
        return (List<T>) List.copyOf(queue);
    }

    /**
     * Removes all elements associated with the specified sink.
     *
     * @param sinkId the identifier of the sink
     */
    public static void clear(UUID sinkId) {
        Objects.requireNonNull(sinkId, "sinkId must not be null");
        ELEMENTS_BY_SINK.remove(sinkId);
    }
}
