package com.flinkboot.test.api.assertion.type;

import com.flinkboot.test.api.sink.CollectingSink;

import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CollectingSinkAssert")
class CollectingSinkAssertTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate successfully with non-null sink")
        void shouldInstantiateWithNonNullSink() {
            try (var sink = new CollectingSink<String>()) {
                assertDoesNotThrow(() -> new CollectingSinkAssert<>(sink));
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when sink is null")
        void shouldThrowExceptionWhenSinkIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new CollectingSinkAssert<>(null));
            assertEquals("sink must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("hasSize")
    class HasSize {

        @Test
        @DisplayName("Should pass when size matches and return same assertion instance for chaining")
        void shouldPassWhenSizeMatchesAndReturnThis() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);
                writer.write("event-2", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.hasSize(2);

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when size does not match")
        void shouldFailWhenSizeDoesNotMatch() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(AssertionFailedError.class, () -> sinkAssert.hasSize(3));

                assertAll(
                    () -> assertTrue(exception.getMessage().contains("Expected sink to have size <3>")),
                    () -> assertTrue(exception.getMessage().contains("was <1>"))
                );
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -5, -100})
        @DisplayName("Should throw IllegalArgumentException when expected size is negative")
        void shouldThrowIllegalArgumentExceptionWhenExpectedSizeIsNegative(int negativeSize) {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(IllegalArgumentException.class, () -> sinkAssert.hasSize(negativeSize));
                assertEquals("expectedSize must not be negative", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("Should pass when sink is empty and return same assertion instance for chaining")
        void shouldPassWhenSinkIsEmptyAndReturnThis() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.isEmpty();

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when sink contains elements")
        void shouldFailWhenSinkContainsElements() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("event-1", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(AssertionFailedError.class, sinkAssert::isEmpty);

                assertTrue(exception.getMessage().contains("Expected sink to be empty, but had size <1>"));
            }
        }
    }

    @Nested
    @DisplayName("contains")
    class Contains {

        @Test
        @DisplayName("Should pass when all expected elements are present and return same assertion instance for chaining")
        void shouldPassWhenAllExpectedElementsArePresent() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("record-A", null);
                writer.write("record-B", null);
                writer.write("record-C", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.contains("record-A", "record-C");

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should pass when expected elements varargs is empty and return this")
        void shouldPassWhenExpectedVarargsIsEmpty() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.contains();

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when expected elements array is null")
        void shouldThrowExceptionWhenExpectedIsNull() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(NullPointerException.class, () -> sinkAssert.contains((String[]) null));
                assertEquals("expected must not be null", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when some or all expected elements are missing")
        void shouldFailWhenExpectedElementsAreMissing() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("present-1", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.contains("present-1", "missing-1", "missing-2")
                );

                assertAll(
                    () -> assertTrue(exception.getMessage().contains("Expected sink to contain [present-1, missing-1, missing-2]")),
                    () -> assertTrue(exception.getMessage().contains("missing: ")),
                    () -> assertTrue(exception.getMessage().contains("missing-1")),
                    () -> assertTrue(exception.getMessage().contains("missing-2"))
                );
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError without throwing NullPointerException when expected element is null")
        void shouldFailWhenExpectedElementIsNull() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("present-1", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.contains("present-1", (String) null)
                );

                assertTrue(exception.getMessage().contains("missing: [null]"));
            }
        }
    }

    @Nested
    @DisplayName("anyMatch")
    class AnyMatch {

        @Test
        @DisplayName("Should pass when at least one element matches predicate and return this")
        void shouldPassWhenAtLeastOneElementMatches() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("alpha-1", null);
                writer.write("beta-2", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.anyMatch(item -> item.startsWith("beta"));

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when no elements match predicate")
        void shouldFailWhenNoElementsMatch() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("alpha-1", null);
                writer.write("beta-2", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.anyMatch(item -> item.startsWith("gamma"))
                );

                assertEquals("Expected at least one element to match predicate, but none did", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when sink is empty")
        void shouldFailWhenSinkIsEmpty() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.anyMatch(item -> true)
                );

                assertEquals("Expected at least one element to match predicate, but none did", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when predicate is null")
        void shouldThrowExceptionWhenPredicateIsNull() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(NullPointerException.class, () -> sinkAssert.anyMatch((Predicate<String>) null));
                assertEquals("predicate must not be null", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("allMatch")
    class AllMatch {

        @Test
        @DisplayName("Should pass when all elements match predicate and return this")
        void shouldPassWhenAllElementsMatch() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("prefix-1", null);
                writer.write("prefix-2", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.allMatch(item -> item.startsWith("prefix"));

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should pass when sink is empty and return this")
        void shouldPassWhenSinkIsEmpty() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.allMatch(item -> item.startsWith("prefix"));

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when any element does not match predicate")
        void shouldFailWhenAnyElementDoesNotMatch() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("prefix-1", null);
                writer.write("other-2", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.allMatch(item -> item.startsWith("prefix"))
                );

                assertAll(
                    () -> assertTrue(exception.getMessage().contains("Expected all elements to match predicate")),
                    () -> assertTrue(exception.getMessage().contains("non-matching elements found: [other-2]"))
                );
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when predicate is null")
        void shouldThrowExceptionWhenPredicateIsNull() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(NullPointerException.class, () -> sinkAssert.allMatch((Predicate<String>) null));
                assertEquals("predicate must not be null", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("noneMatch")
    class NoneMatch {

        @Test
        @DisplayName("Should pass when no elements match predicate and return this")
        void shouldPassWhenNoElementsMatch() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("alpha", null);
                writer.write("beta", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.noneMatch(item -> item.equals("gamma"));

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should pass when sink is empty and return this")
        void shouldPassWhenSinkIsEmpty() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var result = sinkAssert.noneMatch(item -> item.equals("gamma"));

                assertSame(sinkAssert, result);
            }
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError when matching elements are found")
        void shouldFailWhenMatchingElementsFound() throws Exception {
            try (var sink = new CollectingSink<String>()) {
                var writer = sink.createWriter((WriterInitContext) null);
                writer.write("alpha", null);
                writer.write("failed-item", null);

                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> sinkAssert.noneMatch(item -> item.startsWith("failed"))
                );

                assertAll(
                    () -> assertTrue(exception.getMessage().contains("Expected no elements to match predicate")),
                    () -> assertTrue(exception.getMessage().contains("matching elements found: [failed-item]"))
                );
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when predicate is null")
        void shouldThrowExceptionWhenPredicateIsNull() {
            try (var sink = new CollectingSink<String>()) {
                var sinkAssert = new CollectingSinkAssert<>(sink);
                var exception = assertThrows(NullPointerException.class, () -> sinkAssert.noneMatch((Predicate<String>) null));
                assertEquals("predicate must not be null", exception.getMessage());
            }
        }
    }
}
