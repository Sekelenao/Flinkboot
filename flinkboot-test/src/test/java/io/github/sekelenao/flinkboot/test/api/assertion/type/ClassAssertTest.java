package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ClassAssert")
class ClassAssertTest {

    public static class ValidPojo {
        public String name;
        public int count;
    }

    public static class InvalidPojo {
        private String name;
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should instantiate successfully with non-null class")
        void shouldInstantiateWithNonNullClass() {
            assertDoesNotThrow(() -> new ClassAssert(ValidPojo.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when class is null")
        void shouldThrowExceptionWhenClassIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new ClassAssert((Class<?>) null));
            assertEquals("Class to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should instantiate successfully with non-null type hint")
        void shouldInstantiateWithNonNullTypeHint() {
            assertDoesNotThrow(() -> new ClassAssert(new TypeHint<ValidPojo>() {}));
        }

        @Test
        @DisplayName("Should throw NullPointerException when type hint is null")
        void shouldThrowExceptionWhenTypeHintIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new ClassAssert((TypeHint<?>) null));
            assertEquals("TypeHint to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should instantiate successfully with non-null type information")
        void shouldInstantiateWithNonNullTypeInformation() {
            assertDoesNotThrow(() -> new ClassAssert(TypeInformation.of(ValidPojo.class)));
        }

        @Test
        @DisplayName("Should throw NullPointerException when type information is null")
        void shouldThrowExceptionWhenTypeInformationIsNull() {
            var exception = assertThrows(
                NullPointerException.class, () -> new ClassAssert((TypeInformation<?>) null)
            );
            assertEquals("TypeInformation to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("isPojo")
    class IsPojoTests {

        @Test
        @DisplayName("Should pass for valid POJO and return same ClassAssert instance for fluent chaining")
        void shouldPassForValidPojoAndReturnThis() {
            var classAssert = new ClassAssert(ValidPojo.class);
            var result = classAssert.isPojo();
            assertSame(classAssert, result);
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError for invalid POJO")
        void shouldFailForInvalidPojo() {
            var classAssert = new ClassAssert(InvalidPojo.class);
            assertThrows(AssertionFailedError.class, classAssert::isPojo);
        }

        @Test
        @DisplayName("Should pass for valid POJO described by a type hint")
        void shouldPassForValidPojoTypeHint() {
            var classAssert = new ClassAssert(new TypeHint<ValidPojo>() {});
            assertSame(classAssert, classAssert.isPojo());
        }

        @Test
        @DisplayName("Should fail for invalid POJO described by a type hint")
        void shouldFailForInvalidPojoTypeHint() {
            var classAssert = new ClassAssert(new TypeHint<InvalidPojo>() {});
            assertThrows(AssertionFailedError.class, classAssert::isPojo);
        }

        @Test
        @DisplayName("Should pass for valid POJO described by type information")
        void shouldPassForValidPojoTypeInformation() {
            var classAssert = new ClassAssert(TypeInformation.of(ValidPojo.class));
            assertSame(classAssert, classAssert.isPojo());
        }

        @Test
        @DisplayName("Should fail for invalid POJO described by type information")
        void shouldFailForInvalidPojoTypeInformation() {
            var classAssert = new ClassAssert(TypeInformation.of(InvalidPojo.class));
            assertThrows(AssertionFailedError.class, classAssert::isPojo);
        }
    }
}
