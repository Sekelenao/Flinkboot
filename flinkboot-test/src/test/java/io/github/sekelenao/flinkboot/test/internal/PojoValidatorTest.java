package io.github.sekelenao.flinkboot.test.internal;

import io.github.sekelenao.flinkboot.core.api.typing.collection.ListTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.collection.MapTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import org.apache.flink.api.common.serialization.SerializerConfigImpl;
import org.apache.flink.api.common.typeinfo.TypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.EitherTypeInfo;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.types.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
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

    // ==========================================
    // 1. Valid POJO Variants
    // ==========================================

    public static class ValidAnnotatedPojo {
        public String name;
        public int value;

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

    public static class ValidPojoWithGettersSetters {
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

    public static class NestedItemPojo {
        public String itemId;
        public int quantity;
    }

    public static class ValidNestedPojo {
        public String orderId;
        public NestedItemPojo item;

        @TypeInfo(ListTypeInfoFactory.class)
        public List<NestedItemPojo> items;
    }

    public static class TripleContainer<A, B, C> {
        public A first;
        public B second;
        public C third;
    }

    public static class ValidGenericContainerPojo {
        public TripleContainer<String, Integer, Double> triple;
    }

    public static class ParentPojo {
        public String parentId;
    }

    public static class ChildPojo extends ParentPojo {
        public int childValue;

        @TypeInfo(DurationTypeInfoFactory.class)
        public Duration childDuration;
    }

    public static class ValidInheritancePojo {
        public ChildPojo child;
    }

    public static class BoundedContainer<T extends ParentPojo> {
        public T data;
    }

    public static class ValidBoundedContainerPojo {
        public BoundedContainer<ChildPojo> container;
    }

    public static class ValidTuplePojo {
        public Tuple3<String, Integer, Long> tuple;
    }

    public static class ValidObjectArrayPojo {
        public String[] stringArray;
        public NestedItemPojo[] itemArray;
    }

    public static class CustomEitherTypeInfoFactory<L, R> extends TypeInfoFactory<Either<L, R>> {
        @Override
        @SuppressWarnings("unchecked")
        public TypeInformation<Either<L, R>> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
            var left = (TypeInformation<L>) genericParameters.get("L");
            var right = (TypeInformation<R>) genericParameters.get("R");
            return new EitherTypeInfo<>(left, right);
        }
    }

    public static class ValidEitherPojo {
        @TypeInfo(CustomEitherTypeInfoFactory.class)
        public Either<String, Integer> result;
    }

    // ==========================================
    // 2. Invalid POJO Variants (Structural Rules)
    // ==========================================

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

        public String getName() {
            return name;
        }
    }

    @SuppressWarnings("all")
    public static class PrivateFieldOnlySetterPojo {
        private String name;

        public void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("all")
    public static class PrivateAccessorsPojo {
        private String name;

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("all")
    class NonStaticInnerPojo {
        public String name;
    }

    // ==========================================
    // 3. Invalid POJO Variants (Missing @TypeInfo or Unsupported Direct Types)
    // ==========================================

    public static class UnsupportedTypePojo {
        public OffsetDateTime time;
    }

    public static class UnannotatedLocalDateTimePojo {
        public LocalDateTime localDateTime;
    }

    public static class UnannotatedLocalDatePojo {
        public LocalDate localDate;
    }

    public static class UnannotatedLocalTimePojo {
        public LocalTime localTime;
    }

    public static class UnannotatedDurationPojo {
        public Duration duration;
    }

    public static class UnannotatedListPojo {
        public List<String> list;
    }

    public static class UnannotatedMapPojo {
        public Map<String, Integer> map;
    }

    // ==========================================
    // 4. Invalid POJO Variants (Nested & Parameterized Types Falling Back to Kryo)
    // ==========================================

    public static class ListWithUnsupportedElementPojo {
        @TypeInfo(ListTypeInfoFactory.class)
        public List<OffsetDateTime> dates;
    }

    public static class MapWithUnsupportedKeyPojo {
        @TypeInfo(MapTypeInfoFactory.class)
        public Map<OffsetDateTime, String> map;
    }

    public static class MapWithUnsupportedValuePojo {
        @TypeInfo(MapTypeInfoFactory.class)
        public Map<String, OffsetDateTime> map;
    }

    public static class ObjectArrayWithUnsupportedElementPojo {
        public OffsetDateTime[] array;
    }

    public static class TupleWithUnsupportedElementPojo {
        public Tuple3<String, OffsetDateTime, Integer> tuple;
    }

    public static class GenericContainerWithUnsupportedTypePojo {
        public TripleContainer<String, OffsetDateTime, Integer> container;
    }

    public static class ParentWithUnsupportedField {
        public OffsetDateTime unsupportedTime;
    }

    public static class ChildInheritingUnsupportedField extends ParentWithUnsupportedField {
        public String validField;
    }

    public static class WildcardListWithParentPojo {
        @TypeInfo(ListTypeInfoFactory.class)
        public List<? extends ParentPojo> items;
    }

    public static class WildcardListWithUnsupportedTypePojo {
        @TypeInfo(ListTypeInfoFactory.class)
        public List<? extends OffsetDateTime> dates;
    }

    public static class EitherWithUnsupportedLeftPojo {
        @TypeInfo(CustomEitherTypeInfoFactory.class)
        public Either<OffsetDateTime, Integer> result;
    }

    public static class EitherWithUnsupportedRightPojo {
        @TypeInfo(CustomEitherTypeInfoFactory.class)
        public Either<String, OffsetDateTime> result;
    }

    // ==========================================
    // Providers
    // ==========================================

    static Stream<Class<?>> validPojoProvider() {
        return Stream.of(
            ValidAnnotatedPojo.class,
            ValidPojoWithGettersSetters.class,
            ValidNestedPojo.class,
            ValidGenericContainerPojo.class,
            ValidInheritancePojo.class,
            ValidBoundedContainerPojo.class,
            ValidTuplePojo.class,
            ValidObjectArrayPojo.class,
            ValidEitherPojo.class
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
            UnsupportedTypePojo.class,
            UnannotatedLocalDateTimePojo.class,
            UnannotatedLocalDatePojo.class,
            UnannotatedLocalTimePojo.class,
            UnannotatedDurationPojo.class,
            UnannotatedListPojo.class,
            UnannotatedMapPojo.class,
            ListWithUnsupportedElementPojo.class,
            MapWithUnsupportedKeyPojo.class,
            MapWithUnsupportedValuePojo.class,
            ObjectArrayWithUnsupportedElementPojo.class,
            TupleWithUnsupportedElementPojo.class,
            GenericContainerWithUnsupportedTypePojo.class,
            ChildInheritingUnsupportedField.class,
            WildcardListWithParentPojo.class,
            WildcardListWithUnsupportedTypePojo.class,
            EitherWithUnsupportedLeftPojo.class,
            EitherWithUnsupportedRightPojo.class
        );
    }

    // ==========================================
    // Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("validPojoProvider")
    @DisplayName("Should pass validation when class is a valid POJO")
    void shouldPassWhenValidPojo(Class<?> validPojoClass) {
        assertDoesNotThrow(() -> validator.validate(validPojoClass));
    }

    @ParameterizedTest
    @MethodSource("invalidPojoProvider")
    @DisplayName("Should fail validation when class is not a valid POJO or contains unsupported types")
    void shouldFailWhenInvalidPojo(Class<?> invalidPojoClass) {
        assertThrows(AssertionFailedError.class, () -> validator.validate(invalidPojoClass));
    }

    @Test
    @DisplayName("Should throw NullPointerException when class is null")
    void shouldThrowWhenClassIsNull() {
        assertThrows(NullPointerException.class, () -> validator.validate(null));
    }

    @Nested
    @DisplayName("POJO Serialization and Deserialization")
    class SerializationTests {

        @Test
        @DisplayName("Should correctly serialize and deserialize a POJO with all @TypeInfo annotations")
        @SuppressWarnings("unchecked")
        void shouldSerializeAndDeserializeFullAnnotatedPojo() throws IOException {
            var typeInfo = (PojoTypeInfo<ValidAnnotatedPojo>) TypeExtractor.createTypeInfo(ValidAnnotatedPojo.class);
            var serializer = typeInfo.createSerializer(new SerializerConfigImpl());

            var pojo = new ValidAnnotatedPojo();
            pojo.name = "John Doe";
            pojo.value = 42;
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
