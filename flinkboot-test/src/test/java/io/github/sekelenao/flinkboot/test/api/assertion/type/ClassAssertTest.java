package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.TypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClassAssert")
class ClassAssertTest {

    public static class ValidPojo {
        public String name;
        public int count;
    }

    public static class InvalidPojo {
        private String name;
    }

    public static class FailingTypeInfoFactory extends TypeInfoFactory<Object> {
        @Override
        public TypeInformation<Object> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
            throw new InvalidTypesException("Simulated type extraction failure");
        }
    }

    @TypeInfo(FailingTypeInfoFactory.class)
    public static class InvalidTypesExtractionPojo {
        public String value;
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate successfully with non-null class")
        void shouldInstantiateWithNonNullClass() {
            assertDoesNotThrow(() -> new ClassAssert(ValidPojo.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when class is null")
        void shouldThrowExceptionWhenClassIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new ClassAssert(null));
            assertEquals("Class to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("isPojo")
    class IsPojo {

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
        @DisplayName("Should fail with AssertionFailedError and InvalidTypesException cause when type extraction fails")
        void shouldFailWhenTypeExtractionThrowsInvalidTypesException() {
            var classAssert = new ClassAssert(InvalidTypesExtractionPojo.class);
            var error = assertThrows(AssertionFailedError.class, classAssert::isPojo);
            var expectedMessage = "Expected class <" + InvalidTypesExtractionPojo.class.getName() + "> to be a valid POJO, but type extraction failed";
            assertAll(
                () -> assertEquals(expectedMessage, error.getMessage()),
                () -> assertInstanceOf(InvalidTypesException.class, error.getCause()),
                () -> assertEquals("Simulated type extraction failure", error.getCause().getMessage())
            );
        }
    }
}
