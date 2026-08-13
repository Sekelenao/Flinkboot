package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DurationSerializer")
class DurationSerializerTest {

    private final DurationSerializer serializer = DurationSerializer.INSTANCE;

    static Stream<Duration> durationProvider() {
        return Stream.of(
            Duration.ofSeconds(123456L, 789000000),
            Duration.ofSeconds(-987654L, 123000000),
            Duration.ofSeconds(Long.MAX_VALUE, 999999999),
            Duration.ofSeconds(Long.MIN_VALUE, 0),
            Duration.ZERO,
            null
        );
    }

    @Nested
    @DisplayName("Serialization and Deserialization")
    class SerializationAndDeserialization {

        @ParameterizedTest
        @MethodSource("io.github.sekelenao.flinkboot.core.api.typing.time.DurationSerializerTest#durationProvider")
        @DisplayName("Should correctly serialize and deserialize Duration values")
        void shouldSerializeAndDeserialize(Duration original) throws IOException {
            var out = new DataOutputSerializer(128);
            serializer.serialize(original, out);

            var in = new DataInputDeserializer(out.getCopyOfBuffer());
            var deserialized = serializer.deserialize(in);

            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("Should deserialize with reuse instance")
        void shouldDeserializeWithReuse() throws IOException {
            var original = Duration.ofMinutes(42);
            var out = new DataOutputSerializer(128);
            serializer.serialize(original, out);

            var in = new DataInputDeserializer(out.getCopyOfBuffer());
            var deserialized = serializer.deserialize(Duration.ZERO, in);

            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("Should copy serialized data from input view to output view for non-null value")
        void shouldCopyNonNullBetweenViews() throws IOException {
            var original = Duration.ofSeconds(3600, 500);
            var out = new DataOutputSerializer(128);
            serializer.serialize(original, out);

            var in = new DataInputDeserializer(out.getCopyOfBuffer());
            var targetOut = new DataOutputSerializer(128);
            serializer.copy(in, targetOut);

            var targetIn = new DataInputDeserializer(targetOut.getCopyOfBuffer());
            var deserialized = serializer.deserialize(targetIn);

            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("Should copy serialized data from input view to output view for null value")
        void shouldCopyNullBetweenViews() throws IOException {
            var out = new DataOutputSerializer(128);
            serializer.serialize(null, out);

            var in = new DataInputDeserializer(out.getCopyOfBuffer());
            var targetOut = new DataOutputSerializer(128);
            serializer.copy(in, targetOut);

            var targetIn = new DataInputDeserializer(targetOut.getCopyOfBuffer());
            var deserialized = serializer.deserialize(targetIn);

            assertNull(deserialized);
        }

        @Test
        @DisplayName("Should copy duration instance")
        void shouldCopyDurationInstance() {
            var duration = Duration.ofSeconds(100);
            assertAll(
                () -> assertEquals(duration, serializer.copy(duration)),
                () -> assertEquals(duration, serializer.copy(duration, Duration.ZERO))
            );
        }

    }

    @Nested
    @DisplayName("Serializer Properties")
    class SerializerProperties {

        @Test
        @DisplayName("Should report immutable type")
        void shouldBeImmutable() {
            assertTrue(serializer.isImmutableType());
        }

        @Test
        @DisplayName("Should report variable length")
        void shouldHaveVariableLength() {
            assertEquals(-1, serializer.getLength());
        }

        @Test
        @DisplayName("Should duplicate same instance")
        void shouldDuplicateSameInstance() {
            assertEquals(serializer, serializer.duplicate());
        }

        @Test
        @DisplayName("Should create zero instance")
        void shouldCreateZeroInstance() {
            assertEquals(Duration.ZERO, serializer.createInstance());
        }

        @Test
        @DisplayName("Should provide serializer snapshot")
        void shouldSnapshotConfiguration() {
            var snapshot = serializer.snapshotConfiguration();
            assertInstanceOf(DurationSerializerSnapshot.class, snapshot);
        }

        @Test
        @DisplayName("Should implement equals and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            var same = new DurationSerializer();
            assertAll(
                () -> assertEquals(serializer, same),
                () -> assertEquals(serializer.hashCode(), same.hashCode()),
                () -> assertNotEquals(serializer, new Object()),
                () -> assertNotEquals(serializer, null)
            );
        }

    }

}
