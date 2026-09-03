package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeInformationAssert")
class TypeInformationAssertTest {

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
        @DisplayName("Should instantiate successfully with non-null type information")
        void shouldInstantiateWithNonNullTypeInformation() {
            assertDoesNotThrow(() -> new TypeInformationAssert<>(TypeInformation.of(ValidPojo.class)));
        }

        @Test
        @DisplayName("Should throw NullPointerException when type information is null")
        void shouldThrowExceptionWhenTypeInformationIsNull() {
            var exception = assertThrows(
                NullPointerException.class, () -> new TypeInformationAssert<>((TypeInformation<?>) null)
            );
            assertEquals("TypeInformation to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should instantiate successfully with non-null type hint")
        void shouldInstantiateWithNonNullTypeHint() {
            assertDoesNotThrow(() -> new TypeInformationAssert<>(new TypeHint<ValidPojo>() {}));
        }

        @Test
        @DisplayName("Should throw NullPointerException when type hint is null")
        void shouldThrowExceptionWhenTypeHintIsNull() {
            var exception = assertThrows(
                NullPointerException.class, () -> new TypeInformationAssert<>((TypeHint<?>) null)
            );
            assertEquals("TypeHint to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should carry the described type so isPojo keeps it for fluent chaining")
        void shouldCarryDescribedType() {
            TypeInformationAssert<ValidPojo> fromTypeHint = new TypeInformationAssert<>(
                new TypeHint<ValidPojo>() {}
            );
            TypeInformationAssert<ValidPojo> fromTypeInformation = new TypeInformationAssert<>(
                TypeInformation.of(ValidPojo.class)
            );
            assertSame(fromTypeHint, fromTypeHint.isPojo());
            assertSame(fromTypeInformation, fromTypeInformation.isPojo());
        }
    }

    @Nested
    @DisplayName("isPojo")
    class IsPojoTests {

        @Test
        @DisplayName("Should pass for valid POJO described by type information and return this for chaining")
        void shouldPassForValidPojoTypeInformationAndReturnThis() {
            var typeInfoAssert = new TypeInformationAssert<>(TypeInformation.of(ValidPojo.class));
            var result = typeInfoAssert.isPojo();
            assertSame(typeInfoAssert, result);
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError for invalid POJO described by type information")
        void shouldFailForInvalidPojoTypeInformation() {
            var typeInfoAssert = new TypeInformationAssert<>(TypeInformation.of(InvalidPojo.class));
            assertThrows(AssertionFailedError.class, typeInfoAssert::isPojo);
        }

        @Test
        @DisplayName("Should pass for valid POJO described by a type hint")
        void shouldPassForValidPojoTypeHint() {
            var typeInfoAssert = new TypeInformationAssert<>(new TypeHint<ValidPojo>() {});
            assertSame(typeInfoAssert, typeInfoAssert.isPojo());
        }

        @Test
        @DisplayName("Should fail for invalid POJO described by a type hint")
        void shouldFailForInvalidPojoTypeHint() {
            var typeInfoAssert = new TypeInformationAssert<>(new TypeHint<InvalidPojo>() {});
            assertThrows(AssertionFailedError.class, typeInfoAssert::isPojo);
        }

        @Test
        @DisplayName("Should keep generic parameters a Class literal would erase")
        void shouldValidateNestedGenericTypeHint() {
            assertDoesNotThrow(
                () -> new TypeInformationAssert<>(new TypeHint<Tuple2<String, ValidPojo>>() {}).isPojo()
            );
            assertThrows(
                AssertionFailedError.class,
                () -> new TypeInformationAssert<>(new TypeHint<Tuple2<String, InvalidPojo>>() {}).isPojo()
            );
        }
    }
}
