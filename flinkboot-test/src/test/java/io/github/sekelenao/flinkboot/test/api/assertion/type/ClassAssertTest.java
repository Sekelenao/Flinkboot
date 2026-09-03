package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
            var exception = assertThrows(NullPointerException.class, () -> new ClassAssert(null));
            assertEquals("Class to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should expose a single Class based constructor and no generic type entry point")
        void shouldOnlyExposeClassConstructor() {
            var constructors = ClassAssert.class.getConstructors();
            assertEquals(1, constructors.length, "ClassAssert must stay focused on Class<?>");
            assertArrayEquals(new Class<?>[] {Class.class}, constructors[0].getParameterTypes());
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
    }
}
