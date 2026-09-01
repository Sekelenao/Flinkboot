package io.github.sekelenao.flinkboot.test.api.assertion;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlinkbootAssertions API")
class FlinkbootAssertionsTest {

    public static class ValidPojo {
        public String name;
        public int count;
    }

    public static class InvalidPojo {
        private String name;
    }

    @Test
    @DisplayName("Should have private constructor that throws AssertionError to prevent instantiation")
    void shouldPreventInstantiation() throws Exception {
        var constructor = FlinkbootAssertions.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("You cannot instantiate this class"));
    }

    @Nested
    @DisplayName("assertThat(Class<?>)")
    class AssertThatClassTests {

        @Test
        @DisplayName("Should return ClassAssert instance when class is valid")
        void shouldReturnClassAssert() {
            var classAssert = assertThat(ValidPojo.class);
            assertNotNull(classAssert);
        }

        @Test
        @DisplayName("Should throw NullPointerException when target class is null")
        void shouldThrowExceptionWhenClassIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((Class<?>) null));
            assertEquals("Class to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should successfully validate a valid POJO class")
        void shouldValidateValidPojo() {
            assertDoesNotThrow(() -> assertThat(ValidPojo.class).isPojo());
        }

        @Test
        @DisplayName("Should throw AssertionFailedError when class violates POJO rules")
        void shouldFailForInvalidPojo() {
            assertThrows(AssertionFailedError.class, () -> assertThat(InvalidPojo.class).isPojo());
        }
    }

    @Nested
    @DisplayName("assertThat(TypeHint<T>)")
    class AssertThatTypeHintTests {

        @Test
        @DisplayName("Should return ClassAssert instance when type hint is valid")
        void shouldReturnClassAssert() {
            var classAssert = assertThat(new TypeHint<ValidPojo>() {});
            assertNotNull(classAssert);
        }

        @Test
        @DisplayName("Should throw NullPointerException when target type hint is null")
        void shouldThrowExceptionWhenTypeHintIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> assertThat((TypeHint<?>) null));
            assertEquals("TypeHint to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should successfully validate a valid generic POJO type")
        void shouldValidateValidPojo() {
            assertDoesNotThrow(() -> assertThat(new TypeHint<Tuple2<String, ValidPojo>>() {}).isPojo());
        }

        @Test
        @DisplayName("Should throw AssertionFailedError when hinted type violates POJO rules")
        void shouldFailForInvalidPojo() {
            assertThrows(
                AssertionFailedError.class,
                () -> assertThat(new TypeHint<Tuple2<String, InvalidPojo>>() {}).isPojo()
            );
        }
    }

    @Nested
    @DisplayName("assertThat(TypeInformation<T>)")
    class AssertThatTypeInformationTests {

        @Test
        @DisplayName("Should return ClassAssert instance when type information is valid")
        void shouldReturnClassAssert() {
            var classAssert = assertThat(TypeInformation.of(ValidPojo.class));
            assertNotNull(classAssert);
        }

        @Test
        @DisplayName("Should throw NullPointerException when target type information is null")
        void shouldThrowExceptionWhenTypeInformationIsNull() {
            var exception = assertThrows(
                NullPointerException.class, () -> assertThat((TypeInformation<?>) null)
            );
            assertEquals("TypeInformation to assert must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should successfully validate a valid POJO type information")
        void shouldValidateValidPojo() {
            assertDoesNotThrow(() -> assertThat(TypeInformation.of(ValidPojo.class)).isPojo());
        }

        @Test
        @DisplayName("Should throw AssertionFailedError when type information violates POJO rules")
        void shouldFailForInvalidPojo() {
            assertThrows(
                AssertionFailedError.class, () -> assertThat(TypeInformation.of(InvalidPojo.class)).isPojo()
            );
        }
    }
}
