package io.github.sekelenao.flinkboot.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

import static io.github.sekelenao.flinkboot.test.FlinkbootTest.assertPojo;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Assertions")
class FlinkbootTestTest {

    // --- Valid POJO Variant ---
    public static class ValidPojo {
        public String name;
        public int value;
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

    // --- Invalid POJO Variants ---

    // 1. Non-public class
    @SuppressWarnings("all")
    private static class PrivatePojo {
        public String name;
    }

    // 2. Missing default constructor
    @SuppressWarnings("all")
    public static class NoDefaultConstructorPojo {
        public String name;
        public NoDefaultConstructorPojo(String name) {
            this.name = name;
        }
    }

    // 3. Private field with no accessors
    @SuppressWarnings("all")
    public static class PrivateFieldNoAccessorsPojo {
        private String name;
    }

    // 4. Private field with only getter
    @SuppressWarnings("all")
    public static class PrivateFieldOnlyGetterPojo {
        private String name;
        public String getName() { return name; }
    }

    // 5. Private field with only setter
    @SuppressWarnings("all")
    public static class PrivateFieldOnlySetterPojo {
        private String name;
        public void setName(String name) { this.name = name; }
    }

    // 6. Private field with non-public (private) accessors
    @SuppressWarnings("all")
    public static class PrivateAccessorsPojo {
        private String name;
        private String getName() { return name; }
        private void setName(String name) { this.name = name; }
    }

    // 7. Non-static inner class
    @SuppressWarnings("all")
    class NonStaticInnerPojo {
        public String name;
        public NonStaticInnerPojo() {}
    }

    static Stream<Class<?>> validPojoProvider() {
        return Stream.of(
            ValidPojo.class,
            ValidPojoWithGetterSetterAndPrivateField.class
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
            NonStaticInnerPojo.class
        );
    }

    @ParameterizedTest
    @MethodSource("validPojoProvider")
    @DisplayName("Should pass when class is a valid POJO")
    void shouldPassWhenValidPojo(Class<?> validPojoClass) {
        assertPojo(validPojoClass);
    }

    @ParameterizedTest
    @MethodSource("invalidPojoProvider")
    @DisplayName("Should fail when class is not a valid POJO")
    void shouldFailWhenInvalidPojo(Class<?> invalidPojoClass) {
        assertThrows(AssertionFailedError.class, () -> assertPojo(invalidPojoClass));
    }

    @Test
    @DisplayName("Should throw NullPointerException when class is null")
    void shouldThrowExceptionWhenClassIsNull() {
        assertThrows(NullPointerException.class, () -> assertPojo(null));
    }

    @Test
    @DisplayName("Should have private constructor that throws AssertionError to prevent instantiation")
    void shouldPreventInstantiation() throws Exception {
        var constructor = FlinkbootTest.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("You cannot instantiate this class"));
    }

}
