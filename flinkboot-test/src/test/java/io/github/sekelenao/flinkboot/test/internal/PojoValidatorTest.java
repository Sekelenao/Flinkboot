package io.github.sekelenao.flinkboot.test.internal;

import io.github.sekelenao.flinkboot.core.api.typing.time.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.InstantTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import org.apache.flink.api.common.serialization.SerializerConfigImpl;
import org.apache.flink.api.common.typeinfo.TypeInfo;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import io.github.sekelenao.flinkboot.core.api.typing.collection.ListTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.collection.MapTypeInfoFactory;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PojoValidator")
class PojoValidatorTest {

    private PojoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PojoValidator();
    }

    // --- Valid POJO Variants ---
    public static class ValidPojo {
        public String name;
        public int value;

        @TypeInfo(ListTypeInfoFactory.class)
        public List<String> items;

        @TypeInfo(MapTypeInfoFactory.class)
        public Map<String, Integer> mapField;
    }

    public static class ValidPojoWithGetterSetterAndPrivateField {
        private String name;
        private int value;

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int value() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class AnnotatedLocalDateTimePojo {
        @TypeInfo(LocalDateTimeTypeInfoFactory.class)
        public LocalDateTime time;
    }

    public static class AnnotatedJavaTimePojo {
        @TypeInfo(LocalDateTimeTypeInfoFactory.class)
        public LocalDateTime localDateTime;

        @TypeInfo(LocalDateTypeInfoFactory.class)
        public LocalDate localDate;

        @TypeInfo(LocalTimeTypeInfoFactory.class)
        public LocalTime localTime;

        @TypeInfo(InstantTypeInfoFactory.class)
        public Instant instant;

        @TypeInfo(DurationTypeInfoFactory.class)
        public Duration duration;
    }

    public static class FullAnnotatedPojo {
        public String name;
        public int value;

        @TypeInfo(InstantTypeInfoFactory.class)
        public Instant instant;

        @TypeInfo(LocalDateTimeTypeInfoFactory.class)
        public LocalDateTime localDateTime;

        @TypeInfo(LocalDateTypeInfoFactory.class)
        public LocalDate localDate;

        @TypeInfo(LocalTimeTypeInfoFactory.class)
        public LocalTime localTime;

        @TypeInfo(DurationTypeInfoFactory.class)
        public Duration duration;

        @TypeInfo(ListTypeInfoFactory.class)
        public List<String> list;

        @TypeInfo(MapTypeInfoFactory.class)
        public Map<String, Integer> map;

    }

    // --- Invalid POJO Variants ---
    @SuppressWarnings("all")
    private static class PrivatePojo {
        public String name;
    }

    @SuppressWarnings("all")
    public static class NoDefaultConstructorPojo {
        public String name;
        public NoDefaultConstructorPojo(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("all")
    public static class PrivateFieldNoAccessorsPojo {
        private String name;
    }

    @SuppressWarnings("all")
    public static class PrivateFieldOnlyGetterPojo {
        private String name;
        public String getName() { return name; }
    }

    @SuppressWarnings("all")
    public static class PrivateFieldOnlySetterPojo {
        private String name;
        public void setName(String name) { this.name = name; }
    }

    @SuppressWarnings("all")
    public static class PrivateAccessorsPojo {
        private String name;
        private String getName() { return name; }
        private void setName(String name) { this.name = name; }
    }

    @SuppressWarnings("all")
    class NonStaticInnerPojo {
        public String name;
        public NonStaticInnerPojo() {}
    }

    public static class NonPOJOSerializableField {
        public OffsetDateTime time;
    }

    public static class UnannotatedLocalDateTimePojo {
        public LocalDateTime time;
    }

    public static class UnannotatedDurationPojo {
        public Duration duration;
    }

    public static class UnannotatedListPojo {
        public List<String> listField;
    }

    public static class UnannotatedMapPojo {
        public Map<String, Integer> mapField;
    }

    static Stream<Class<?>> validPojoProvider() {
        return Stream.of(
            ValidPojo.class,
            ValidPojoWithGetterSetterAndPrivateField.class,
            AnnotatedLocalDateTimePojo.class,
            AnnotatedJavaTimePojo.class,
            FullAnnotatedPojo.class
        );
    }

    static Stream<Class<?>> invalidPojoProvider() {
        return Stream.of(
            PrivatePojo.class,
            NoDefaultConstructorPojo.class,
            PrivateFieldNoAccessorsPojo.class,
            PrivateFieldOnlyGetterPojo.class,
            PrivateFieldOnlySetterPojo.class,
            PrivateAccessorsPojo.class,
            NonStaticInnerPojo.class,
            NonPOJOSerializableField.class,
            UnannotatedLocalDateTimePojo.class,
            UnannotatedDurationPojo.class,
            UnannotatedListPojo.class,
            UnannotatedMapPojo.class
        );
    }

    @ParameterizedTest
    @MethodSource("validPojoProvider")
    @DisplayName("Should pass when class is a valid POJO")
    void shouldPassWhenValidPojo(Class<?> validPojoClass) {
        assertDoesNotThrow(() -> validator.validate(validPojoClass));
    }

    @ParameterizedTest
    @MethodSource("invalidPojoProvider")
    @DisplayName("Should fail when class is not a valid POJO")
    void shouldFailWhenInvalidPojo(Class<?> invalidPojoClass) {
        assertThrows(AssertionFailedError.class, () -> validator.validate(invalidPojoClass));
    }

    @Test
    @DisplayName("Non POJO serializable fields fail validation")
    void nonPojoField() {
        assertThrows(AssertionFailedError.class, () -> validator.validate(NonPOJOSerializableField.class));
    }

    @Test
    @DisplayName("LocalDateTime field fails when not annotated with @TypeInfo")
    void localDateTimeFailsWhenUnannotated() {
        assertThrows(AssertionFailedError.class, () -> validator.validate(UnannotatedLocalDateTimePojo.class));
    }

    @Test
    @DisplayName("LocalDateTime field passes when annotated with @TypeInfo")
    void localDateTimePassesWhenAnnotated() {
        assertDoesNotThrow(() -> validator.validate(AnnotatedLocalDateTimePojo.class));
    }

    @Test
    @DisplayName("Should throw NullPointerException when class is null")
    void shouldThrowForNullInputs() {
        assertThrows(NullPointerException.class, () -> validator.validate(null));
    }

    @Nested
    @DisplayName("POJO Serialization and Deserialization")
    class SerializationTests {

        @Test
        @DisplayName("Should correctly serialize and deserialize a POJO with all @TypeInfo annotations")
        void shouldSerializeAndDeserializeFullAnnotatedPojo() throws IOException {
            var typeInfo = (PojoTypeInfo<FullAnnotatedPojo>) TypeExtractor.createTypeInfo(FullAnnotatedPojo.class);
            var serializer = typeInfo.createSerializer(new SerializerConfigImpl());

            var pojo = new FullAnnotatedPojo();
            pojo.name = "John Doe";
            pojo.value = 42;
            pojo.instant = Instant.parse("2026-08-13T12:00:00Z");
            pojo.localDateTime = LocalDateTime.parse("2026-08-13T14:30:00");
            pojo.localDate = LocalDate.parse("2026-08-13");
            pojo.localTime = LocalTime.parse("14:30:00");
            pojo.duration = Duration.ofMinutes(15);
            pojo.list = List.of("alpha", "beta", "gamma");
            pojo.map = Map.of("k1", 100, "k2", 200);

            var out = new DataOutputSerializer(256);
            serializer.serialize(pojo, out);

            var in = new DataInputDeserializer(out.getCopyOfBuffer());
            var deserialized = serializer.deserialize(in);

            assertAll(
                () -> assertEquals(pojo.name, deserialized.name),
                () -> assertEquals(pojo.value, deserialized.value),
                () -> assertEquals(pojo.instant, deserialized.instant),
                () -> assertEquals(pojo.localDateTime, deserialized.localDateTime),
                () -> assertEquals(pojo.localDate, deserialized.localDate),
                () -> assertEquals(pojo.localTime, deserialized.localTime),
                () -> assertEquals(pojo.duration, deserialized.duration),
                () -> assertEquals(pojo.list, deserialized.list),
                () -> assertEquals(pojo.map, deserialized.map)
            );
        }

    }
}
