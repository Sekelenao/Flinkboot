package com.flinkboot.test.api.assertion;

import com.flinkboot.test.api.assertion.type.ClassAssert;
import com.flinkboot.test.api.assertion.type.CollectingSinkAssert;
import com.flinkboot.test.api.assertion.type.ObjectAssert;
import com.flinkboot.test.api.assertion.type.TypeInformationAssert;
import com.flinkboot.test.api.sink.CollectingSink;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.flinkboot.test.api.assertion.FlinkbootAssertions.assertThat;
import static com.flinkboot.test.api.assertion.FlinkbootAssertions.assertThatObject;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlinkbootAssertions API")
class FlinkbootAssertionsTest {

    public static class ValidPojo {
        public String name;
        public int count;
    }

    static class SerializableSample implements Serializable {
        private final String value = "sample";
    }

    @Test
    @DisplayName("Should have private constructor that throws AssertionError to prevent instantiation")
    void shouldPreventInstantiation() throws Exception {
        var constructor = FlinkbootAssertions.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("assertThat(Class<?>)")
    class AssertThatClass {

        @Test
        @DisplayName("Should return ClassAssert instance when class is valid")
        void shouldReturnClassAssert() {
            assertInstanceOf(ClassAssert.class, assertThat(ValidPojo.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when target class is null")
        void shouldThrowExceptionWhenClassIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((Class<?>) null));
            assertEquals("Class to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("assertThat(Object)")
    class AssertThatObject {

        @Test
        @DisplayName("Should return ObjectAssert instance when object is valid")
        void shouldReturnObjectAssert() {
            assertInstanceOf(ObjectAssert.class, assertThat(new SerializableSample()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when target object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((Object) null));
            assertEquals("Object to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("assertThat(CollectingSink)")
    class AssertThatCollectingSink {

        @Test
        @DisplayName("Should return CollectingSinkAssert instance when sink is valid")
        void shouldReturnCollectingSinkAssert() {
            try (var sink = new CollectingSink<String>()) {
                assertInstanceOf(CollectingSinkAssert.class, assertThat(sink));
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when sink is null")
        void shouldThrowExceptionWhenSinkIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((CollectingSink<String>) null));
            assertEquals("sink must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("assertThat(TypeHint<T>)")
    class AssertThatTypeHint {

        @Test
        @DisplayName("Should return TypeInformationAssert instance when type hint is valid")
        void shouldReturnTypeInformationAssert() {
            TypeInformationAssert<ValidPojo> typeInfoAssert = assertThat(new TypeHint<ValidPojo>() {});
            assertInstanceOf(TypeInformationAssert.class, typeInfoAssert);
        }

        @Test
        @DisplayName("Should throw NullPointerException when target type hint is null")
        void shouldThrowExceptionWhenTypeHintIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((TypeHint<?>) null));
            assertEquals("TypeHint to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("assertThat(TypeInformation<T>)")
    class AssertThatTypeInformation {

        @Test
        @DisplayName("Should return TypeInformationAssert instance when type information is valid")
        void shouldReturnTypeInformationAssert() {
            TypeInformationAssert<ValidPojo> typeInfoAssert = assertThat(TypeInformation.of(ValidPojo.class));
            assertInstanceOf(TypeInformationAssert.class, typeInfoAssert);
        }

        @Test
        @DisplayName("Should throw NullPointerException when target type information is null")
        void shouldThrowExceptionWhenTypeInformationIsNull() {
            var exception = assertThrows(
                NullPointerException.class, () -> assertThat((TypeInformation<?>) null)
            );
            assertEquals("TypeInformation to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("assertThatObject(T)")
    class AssertThatObjectExplicit {

        @Test
        @DisplayName("Should throw NullPointerException when target object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThatObject(null));
            assertEquals("Object to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate serializable objects including types shadowed by specific assertThat overloads")
        void shouldValidateSerializableObjectsIncludingShadowedTypes() {
            try (var sink = new CollectingSink<String>()) {
                var typeInfo = TypeInformation.of(ValidPojo.class);
                assertAll(
                    () -> assertDoesNotThrow(() -> assertThatObject(new SerializableSample()).isSerializable()),
                    () -> assertDoesNotThrow(() -> assertThatObject(typeInfo).isSerializable()),
                    () -> assertDoesNotThrow(() -> assertThatObject(sink).isSerializable()),
                    () -> assertDoesNotThrow(() -> assertThatObject(ValidPojo.class).isSerializable())
                );
            }
        }

        @Test
        @DisplayName("Should throw AssertionFailedError when a shadowed type is not serializable")
        void shouldFailForNonSerializableShadowedType() {
            var typeHint = new TypeHint<ValidPojo>() {};
            assertThrows(
                AssertionFailedError.class,
                () -> assertThatObject(typeHint).isSerializable()
            );
        }
    }
}
