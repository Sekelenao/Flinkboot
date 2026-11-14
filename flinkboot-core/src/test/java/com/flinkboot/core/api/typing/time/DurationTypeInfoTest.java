package com.flinkboot.core.api.typing.time;

import com.flinkboot.core.internal.time.DurationComparator;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.SerializerConfigImpl;
import org.apache.flink.api.common.typeinfo.AtomicType;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DurationTypeInfo")
class DurationTypeInfoTest {

    private final DurationTypeInfo typeInfo = DurationTypeInfo.INSTANCE;

    @Nested
    @DisplayName("Type Information Properties")
    class TypeProperties {

        @Test
        @DisplayName("Should verify all type properties")
        void shouldVerifyTypeProperties() {
            assertAll(
                () -> assertTrue(typeInfo.isBasicType()),
                () -> assertFalse(typeInfo.isTupleType()),
                () -> assertTrue(typeInfo.isKeyType()),
                () -> assertEquals(1, typeInfo.getArity()),
                () -> assertEquals(1, typeInfo.getTotalFields()),
                () -> assertEquals(Duration.class, typeInfo.getTypeClass()),
                () -> assertEquals("Duration", typeInfo.toString())
            );
        }
    }

    @Nested
    @DisplayName("Serializer Creation")
    class SerializerCreation {

        @Test
        @DisplayName("Should create DurationSerializer with modern SerializerConfig")
        void shouldCreateSerializerWithSerializerConfig() {
            var serializer = typeInfo.createSerializer(new SerializerConfigImpl());
            assertSame(DurationSerializer.INSTANCE, serializer);
        }

        @Test
        @SuppressWarnings({"deprecation", "removal"})
        @DisplayName("Should create DurationSerializer with legacy ExecutionConfig")
        void shouldCreateSerializerWithExecutionConfig() {
            var serializer = typeInfo.createSerializer(new ExecutionConfig());
            assertSame(DurationSerializer.INSTANCE, serializer);
        }
    }

    @Nested
    @DisplayName("Comparator Creation")
    class ComparatorCreation {

        @Test
        @DisplayName("Should implement AtomicType and create DurationComparator with requested sort order")
        void shouldCreateComparatorWithRequestedSortOrder() {
            var config = new ExecutionConfig();
            var ascending = typeInfo.createComparator(true, config);
            var descending = typeInfo.createComparator(false, config);
            var smaller = Duration.ofSeconds(1);
            var larger = Duration.ofSeconds(5);

            assertAll(
                () -> assertInstanceOf(AtomicType.class, typeInfo),
                () -> assertInstanceOf(DurationComparator.class, ascending),
                () -> assertInstanceOf(DurationComparator.class, descending),
                () -> assertTrue(ascending.compare(smaller, larger) < 0),
                () -> assertTrue(descending.compare(smaller, larger) > 0)
            );
        }

        @Test
        @DisplayName("Should support Flink DataStream keyBy with Duration keys")
        void shouldSupportKeyByWithDurationKeys() {
            var env = StreamExecutionEnvironment.getExecutionEnvironment();
            var keyed = env.fromData(
                    DurationTypeInfo.INSTANCE,
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2),
                    Duration.ofSeconds(1))
                .keyBy(duration -> duration, DurationTypeInfo.INSTANCE);
            keyed.reduce(Duration::plus).print();

            var streamGraph = env.getStreamGraph();

            assertAll(
                () -> assertSame(DurationTypeInfo.INSTANCE, keyed.getKeyType()),
                () -> assertFalse(streamGraph.getStreamNodes().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should implement equals, canEqual and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            var same = new DurationTypeInfo();
            assertAll(
                () -> assertTrue(typeInfo.canEqual(same)),
                () -> assertFalse(typeInfo.canEqual(new Object())),
                () -> assertEquals(typeInfo, same),
                () -> assertEquals(DurationTypeInfo.class.hashCode(), typeInfo.hashCode()),
                () -> assertEquals(typeInfo.hashCode(), same.hashCode()),
                () -> assertNotEquals(typeInfo, new Object()),
                () -> assertNotEquals(typeInfo, null)
            );
        }
    }

}
