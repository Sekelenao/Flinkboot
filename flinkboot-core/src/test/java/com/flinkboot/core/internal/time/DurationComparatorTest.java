package com.flinkboot.core.internal.time;

import com.flinkboot.core.api.typing.time.DurationSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DurationComparator")
class DurationComparatorTest {

    private static DataInputDeserializer serialize(Duration duration) throws IOException {
        var out = new DataOutputSerializer(16);
        DurationSerializer.INSTANCE.serialize(duration, out);
        return new DataInputDeserializer(out.getCopyOfBuffer());
    }

    private static byte[] normalizedKey(DurationComparator comparator, Duration value, int len) {
        var segment = MemorySegmentFactory.allocateUnpooledSegment(Math.max(len, 1));
        comparator.putNormalizedKey(value, segment, 0, len);
        var bytes = new byte[len];
        segment.get(0, bytes, 0, len);
        return bytes;
    }

    @Nested
    @DisplayName("In-Memory Comparison")
    class InMemoryComparison {

        @ParameterizedTest
        @CsvSource({
            "-10, 0, -5, 0, -1",
            "0, 0, 0, 0, 0",
            "5, 0, 10, 0, -1",
            "10, 0, 5, 0, 1",
            "5, 100, 5, 200, -1",
            "5, 200, 5, 100, 1",
            "5, 100, 5, 100, 0"
        })
        @DisplayName("Should compare durations in ascending order")
        void shouldCompareAscending(
                long firstSeconds, int firstNanos,
                long secondSeconds, int secondNanos,
                int expectedSign) {
            var comparator = new DurationComparator(true);
            var first = Duration.ofSeconds(firstSeconds, firstNanos);
            var second = Duration.ofSeconds(secondSeconds, secondNanos);

            assertEquals(expectedSign, Integer.signum(comparator.compare(first, second)));
        }

        @Test
        @DisplayName("Should invert comparison result in descending order")
        void shouldCompareDescending() {
            var comparator = new DurationComparator(false);
            var smaller = Duration.ofSeconds(1);
            var larger = Duration.ofSeconds(5);

            assertAll(
                () -> assertTrue(comparator.compare(smaller, larger) > 0),
                () -> assertTrue(comparator.compare(larger, smaller) < 0),
                () -> assertEquals(0, comparator.compare(smaller, Duration.ofSeconds(1)))
            );
        }
    }

    @Nested
    @DisplayName("Serialized Comparison")
    class SerializedComparison {

        @ParameterizedTest
        @CsvSource({
            "-10, 0, -5, 0, -1",
            "0, 0, 0, 0, 0",
            "5, 0, 10, 0, -1",
            "10, 0, 5, 0, 1",
            "5, 100, 5, 200, -1",
            "5, 200, 5, 100, 1",
            "5, 100, 5, 100, 0"
        })
        @DisplayName("Should compare serialized durations in ascending order")
        void shouldCompareSerializedAscending(
                long firstSeconds, int firstNanos,
                long secondSeconds, int secondNanos,
                int expectedSign) throws IOException {
            var comparator = new DurationComparator(true);
            var first = serialize(Duration.ofSeconds(firstSeconds, firstNanos));
            var second = serialize(Duration.ofSeconds(secondSeconds, secondNanos));

            assertEquals(expectedSign, Integer.signum(comparator.compareSerialized(first, second)));
        }

        @Test
        @DisplayName("Should invert serialized comparison in descending order")
        void shouldCompareSerializedDescending() throws IOException {
            var comparator = new DurationComparator(false);

            assertAll(
                () -> assertTrue(comparator.compareSerialized(
                    serialize(Duration.ofSeconds(1)),
                    serialize(Duration.ofSeconds(5))) > 0),
                () -> assertTrue(comparator.compareSerialized(
                    serialize(Duration.ofSeconds(5)),
                    serialize(Duration.ofSeconds(1))) < 0),
                () -> assertEquals(0, comparator.compareSerialized(
                    serialize(Duration.ofSeconds(5, 42)),
                    serialize(Duration.ofSeconds(5, 42))))
            );
        }

        @Test
        @DisplayName("Should handle null values consistently in serialized comparison")
        void shouldHandleNullsInSerializedComparison() throws IOException {
            var ascending = new DurationComparator(true);
            var descending = new DurationComparator(false);
            var nonNull = Duration.ofSeconds(1);

            assertAll(
                () -> assertEquals(0, ascending.compareSerialized(serialize(null), serialize(null))),
                () -> assertTrue(ascending.compareSerialized(serialize(null), serialize(nonNull)) < 0),
                () -> assertTrue(ascending.compareSerialized(serialize(nonNull), serialize(null)) > 0),
                () -> assertEquals(0, descending.compareSerialized(serialize(null), serialize(null))),
                () -> assertTrue(descending.compareSerialized(serialize(null), serialize(nonNull)) > 0),
                () -> assertTrue(descending.compareSerialized(serialize(nonNull), serialize(null)) < 0)
            );
        }
    }

    @Nested
    @DisplayName("Reference and Hash Operations")
    class ReferenceAndHash {

        @Test
        @DisplayName("Should compute hash and compare against reference")
        void shouldComputeHashAndCompareAgainstReference() {
            var comparator = new DurationComparator(true);
            var otherComparator = new DurationComparator(true);
            var ref = Duration.ofSeconds(10);
            var larger = Duration.ofSeconds(20);

            comparator.setReference(ref);
            otherComparator.setReference(larger);

            assertAll(
                () -> assertEquals(ref.hashCode(), comparator.hash(ref)),
                () -> assertTrue(comparator.equalToReference(Duration.ofSeconds(10))),
                () -> assertFalse(comparator.equalToReference(larger)),
                () -> assertTrue(comparator.compareToReference(otherComparator) > 0)
            );
        }
    }

    @Nested
    @DisplayName("Normalized Keys and Duplication")
    class NormalizedKeysAndDuplication {

        @Test
        @DisplayName("Should report normalized key metadata and inversion")
        void shouldReportNormalizedKeyMetadata() {
            var ascending = new DurationComparator(true);
            var descending = new DurationComparator(false);

            assertAll(
                () -> assertTrue(ascending.supportsNormalizedKey()),
                () -> assertEquals(13, ascending.getNormalizeKeyLen()),
                () -> assertTrue(ascending.isNormalizedKeyPrefixOnly(12)),
                () -> assertFalse(ascending.isNormalizedKeyPrefixOnly(13)),
                () -> assertFalse(ascending.isNormalizedKeyPrefixOnly(16)),
                () -> assertFalse(ascending.invertNormalizedKey()),
                () -> assertTrue(descending.invertNormalizedKey())
            );
        }

        @Test
        @DisplayName("Should produce unsigned-ordered normalized keys across full range")
        void shouldProduceOrderedNormalizedKeys() {
            var comparator = new DurationComparator(true);
            var keyNull = normalizedKey(comparator, null, 13);
            var keyMin = normalizedKey(comparator, Duration.ofSeconds(Long.MIN_VALUE), 13);
            var keyNeg = normalizedKey(comparator, Duration.ofSeconds(-5), 13);
            var keyZero = normalizedKey(comparator, Duration.ZERO, 13);
            var keyNanoLow = normalizedKey(comparator, Duration.ofSeconds(5, 100), 13);
            var keyNanoHigh = normalizedKey(comparator, Duration.ofSeconds(5, 200), 13);
            var keyMax = normalizedKey(comparator, Duration.ofSeconds(Long.MAX_VALUE, 999_999_999), 13);

            assertAll(
                () -> assertTrue(Arrays.compareUnsigned(keyNull, keyMin) < 0),
                () -> assertTrue(Arrays.compareUnsigned(keyMin, keyNeg) < 0),
                () -> assertTrue(Arrays.compareUnsigned(keyNeg, keyZero) < 0),
                () -> assertTrue(Arrays.compareUnsigned(keyZero, keyNanoLow) < 0),
                () -> assertTrue(Arrays.compareUnsigned(keyNanoLow, keyNanoHigh) < 0),
                () -> assertTrue(Arrays.compareUnsigned(keyNanoHigh, keyMax) < 0)
            );
        }

        @Test
        @DisplayName("Should handle truncated, zero-length, and padded normalized key lengths")
        void shouldHandleVariousNormalizedKeyLengths() {
            var comparator = new DurationComparator(true);
            var first = Duration.ofSeconds(5, 0x01020304);
            var second = Duration.ofSeconds(5, 0x01020305);

            var segment = MemorySegmentFactory.allocateUnpooledSegment(16);
            segment.put(0, (byte) 0x7F);
            comparator.putNormalizedKey(first, segment, 0, 0);
            byte untouchedByte = segment.get(0);

            byte[] prefixSecondsA = normalizedKey(comparator, Duration.ofSeconds(-1), 5);
            byte[] prefixSecondsB = normalizedKey(comparator, Duration.ofSeconds(1), 5);

            byte[] prefixNanosA = normalizedKey(comparator, first, 11);
            byte[] prefixNanosB = normalizedKey(comparator, second, 11);

            byte[] padded = normalizedKey(comparator, first, 16);

            assertAll(
                () -> assertEquals((byte) 0x7F, untouchedByte),
                () -> assertTrue(Arrays.compareUnsigned(prefixSecondsA, prefixSecondsB) < 0),
                () -> assertEquals(0, Arrays.compareUnsigned(prefixNanosA, prefixNanosB)),
                () -> assertEquals(0, padded[13]),
                () -> assertEquals(0, padded[14]),
                () -> assertEquals(0, padded[15])
            );
        }

        @Test
        @DisplayName("Should duplicate comparator preserving sort order")
        void shouldDuplicatePreservingSortOrder() throws IOException {
            var descending = new DurationComparator(false);
            var copy = descending.duplicate();

            assertAll(
                () -> assertNotSame(descending, copy),
                () -> assertTrue(copy.compare(Duration.ofSeconds(1), Duration.ofSeconds(5)) > 0),
                () -> assertTrue(copy.compareSerialized(
                    serialize(Duration.ofSeconds(1)),
                    serialize(Duration.ofSeconds(5))) > 0)
            );
        }
    }
}
