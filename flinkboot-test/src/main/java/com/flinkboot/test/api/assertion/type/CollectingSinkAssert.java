package com.flinkboot.test.api.assertion.type;

import com.flinkboot.test.api.sink.CollectingSink;

import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Fluent assertion provider for verifying records collected by a {@link CollectingSink}.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * CollectingSink<OrderEvent> sink = new CollectingSink<>();
 * // ... run Flink pipeline ...
 *
 * FlinkbootAssertions.assertThat(sink)
 *     .hasSize(2)
 *     .anyMatch(order -> order.status().equals("COMPLETED"))
 *     .allMatch(order -> order.amount() > 0);
 * }</pre>
 *
 * @param <T> the type of elements collected by the sink
 */
public final class CollectingSinkAssert<T> {

    private final CollectingSink<T> sink;

    /**
     * Creates a new {@link CollectingSinkAssert} for the given collecting sink.
     *
     * @param sink the collecting sink to assert
     * @throws NullPointerException if {@code sink} is {@code null}
     */
    public CollectingSinkAssert(CollectingSink<T> sink) {
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
    }

    /**
     * Asserts that the number of collected elements equals {@code expectedSize}.
     *
     * @param expectedSize the expected number of collected elements
     * @return this assertion object for method chaining
     * @throws IllegalArgumentException if {@code expectedSize} is negative
     * @throws AssertionError if the number of collected elements does not match {@code expectedSize}
     */
    public CollectingSinkAssert<T> hasSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize must not be negative");
        }
        var actualSize = sink.size();
        Assertions.assertEquals(
            expectedSize,
            actualSize,
            () -> "Expected sink to have size <" + expectedSize + ">, but was <" + actualSize + ">"
        );
        return this;
    }

    /**
     * Asserts that no elements have been collected by this sink.
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the sink is not empty
     */
    public CollectingSinkAssert<T> isEmpty() {
        var actualSize = sink.size();
        Assertions.assertEquals(
            0,
            actualSize,
            () -> "Expected sink to be empty, but had size <" + actualSize + ">"
        );
        return this;
    }

    /**
     * Asserts that at least one element has been collected by this sink.
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the sink is empty
     */
    public CollectingSinkAssert<T> isNotEmpty() {
        if (sink.isEmpty()) {
            Assertions.fail("Expected sink not to be empty, but was empty");
        }
        return this;
    }

    /**
     * Asserts that all specified elements are present in the collected elements.
     *
     * @param expected the elements expected to be contained in the sink
     * @return this assertion object for method chaining
     * @throws NullPointerException if {@code expected} is {@code null}
     * @throws AssertionError if any of the expected elements are missing
     */
    @SafeVarargs
    public final CollectingSinkAssert<T> contains(T... expected) {
        Objects.requireNonNull(expected, "expected must not be null");
        if (expected.length == 0) {
            return this;
        }
        var actualSet = new HashSet<>(sink.elements());
        var missing = Arrays.stream(expected)
            .filter(item -> item == null || !actualSet.contains(item))
            .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            Assertions.fail(
                "Expected sink to contain " + Arrays.toString(expected) + ", but missing: " + missing
            );
        }
        return this;
    }

    /**
     * Asserts that at least one collected element matches the given predicate.
     *
     * @param predicate condition to test against collected elements
     * @return this assertion object for method chaining
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws AssertionError if no elements match the predicate
     */
    public CollectingSinkAssert<T> anyMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        var elements = sink.elements();
        if (elements.stream().noneMatch(predicate)) {
            Assertions.fail("Expected at least one element to match predicate, but none did");
        }
        return this;
    }

    /**
     * Asserts that all collected elements match the given predicate.
     * <p>
     * If the sink contains zero elements, this assertion is trivially satisfied (vacuous truth)
     * because no elements violate the predicate. If non-emptiness is required, chain with
     * {@link #isNotEmpty()} (e.g. {@code assertThat(sink).isNotEmpty().allMatch(predicate)}).
     * </p>
     *
     * @param predicate condition to test against collected elements
     * @return this assertion object for method chaining
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws AssertionError if any element fails to match the predicate
     */
    public CollectingSinkAssert<T> allMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        var elements = sink.elements();
        var nonMatching = elements.stream()
            .filter(predicate.negate())
            .collect(Collectors.toSet());
        if (!nonMatching.isEmpty()) {
            Assertions.fail(
                "Expected all elements to match predicate, but non-matching elements found: " + nonMatching
            );
        }
        return this;
    }

    /**
     * Asserts that no collected elements match the given predicate.
     * <p>
     * If the sink contains zero elements, this assertion is trivially satisfied (vacuous truth)
     * because no elements satisfy the predicate. If non-emptiness is required, chain with
     * {@link #isNotEmpty()} (e.g. {@code assertThat(sink).isNotEmpty().noneMatch(predicate)}).
     * </p>
     *
     * @param predicate condition to test against collected elements
     * @return this assertion object for method chaining
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws AssertionError if any element matches the predicate
     */
    public CollectingSinkAssert<T> noneMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        var elements = sink.elements();
        var matching = elements.stream()
            .filter(predicate)
            .collect(Collectors.toSet());
        if (!matching.isEmpty()) {
            Assertions.fail(
                "Expected no elements to match predicate, but matching elements found: " + matching
            );
        }
        return this;
    }
}
