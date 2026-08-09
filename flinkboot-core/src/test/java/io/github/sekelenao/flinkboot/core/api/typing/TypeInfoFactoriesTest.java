package io.github.sekelenao.flinkboot.core.api.typing;

import io.github.sekelenao.flinkboot.core.api.typing.duration.DurationSerializer;
import io.github.sekelenao.flinkboot.core.api.typing.duration.DurationTypeInfo;
import io.github.sekelenao.flinkboot.core.api.typing.duration.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.instant.InstantTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.local.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.local.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.local.LocalTimeTypeInfoFactory;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import java.time.Duration;
import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeInfoFactoriesTest {

    @Test
    @DisplayName("LocalDateTimeTypeInfoFactory returns Types.LOCAL_DATE_TIME")
    void testLocalDateTimeTypeInfoFactory() {
        var factory = new LocalDateTimeTypeInfoFactory();
        assertEquals(Types.LOCAL_DATE_TIME, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("LocalDateTypeInfoFactory returns Types.LOCAL_DATE")
    void testLocalDateTypeInfoFactory() {
        var factory = new LocalDateTypeInfoFactory();
        assertEquals(Types.LOCAL_DATE, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("LocalTimeTypeInfoFactory returns Types.LOCAL_TIME")
    void testLocalTimeTypeInfoFactory() {
        var factory = new LocalTimeTypeInfoFactory();
        assertEquals(Types.LOCAL_TIME, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("InstantTypeInfoFactory returns Types.INSTANT")
    void testInstantTypeInfoFactory() {
        var factory = new InstantTypeInfoFactory();
        assertEquals(Types.INSTANT, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("DurationTypeInfoFactory returns DurationTypeInfo.INSTANCE")
    void testDurationTypeInfoFactory() {
        var factory = new DurationTypeInfoFactory();
        assertEquals(DurationTypeInfo.INSTANCE, factory.createTypeInfo(null, Collections.emptyMap()));
    }

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

    @ParameterizedTest
    @MethodSource("durationProvider")
    @DisplayName("DurationSerializer correctly serializes and deserializes Duration values")
    void testDurationSerialization(Duration original) throws Exception {
        var serializer = DurationSerializer.INSTANCE;
        var out = new DataOutputSerializer(128);

        serializer.serialize(original, out);

        var in = new DataInputDeserializer(out.getCopyOfBuffer());
        var deserialized = serializer.deserialize(in);

        assertEquals(original, deserialized);
    }

}
