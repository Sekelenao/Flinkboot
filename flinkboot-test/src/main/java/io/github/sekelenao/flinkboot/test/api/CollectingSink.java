package io.github.sekelenao.flinkboot.test.api;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A sink for collecting elements in unit tests.
 *
 * @param <T> the type of elements collected by this sink
 */
@SuppressWarnings("deprecation")
public final class CollectingSink<T> implements SinkFunction<T> {

    private static final Map<UUID, List<Object>> ELEMENTS_BY_SINK = new ConcurrentHashMap<>();

    private final UUID sinkId = UUID.randomUUID();

    private CollectingSink() {
    }

    /**
     * Creates a collecting sink.
     *
     * @param <T> the type of elements collected by the sink
     * @return a new collecting sink
     */
    public static <T> CollectingSink<T> create() {
        return new CollectingSink<>();
    }

    /**
     * Collects an element.
     *
     * @param value the element to collect
     */
    @Override
    public void invoke(T value) {
        elementsForSink().add(Objects.requireNonNull(value, "value must not be null"));
    }

    /**
     * Returns an immutable snapshot of the collected elements.
     *
     * @return the collected elements
     */
    public List<T> elements() {
        return List.copyOf(elementsForSink());
    }

    /**
     * Removes all collected elements.
     */
    public void clear() {
        ELEMENTS_BY_SINK.remove(sinkId);
    }

    @SuppressWarnings("unchecked")
    private List<T> elementsForSink() {
        return (List<T>) (List<?>) ELEMENTS_BY_SINK.computeIfAbsent(
            sinkId,
            ignored -> new CopyOnWriteArrayList<>()
        );
    }
}
