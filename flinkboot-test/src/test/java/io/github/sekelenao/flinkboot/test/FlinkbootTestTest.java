package io.github.sekelenao.flinkboot.test;

import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Files;

import java.util.List;
import java.util.stream.Stream;

import static io.github.sekelenao.flinkboot.test.FlinkbootTest.assertPojo;
import static io.github.sekelenao.flinkboot.test.FlinkbootTest.configuration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Nested
    @DisplayName("Configuration Helper")
    class ConfigurationHelperTests {

        @Test
        @DisplayName("Should load configuration directly from single temp YAML file using varargs")
        void shouldLoadConfigurationFromSingleFile() throws Exception {
            var tempFile = Files.createTempFile("flinkboot-test-", ".yaml");
            tempFile.toFile().deleteOnExit();
            Files.writeString(tempFile, "name: test-job-single-file\n");

            var jobProperties = configuration(
                JobProperties.class,
                "file:" + tempFile.toAbsolutePath()
            );

            assertAll(
                () -> assertNotNull(jobProperties),
                () -> assertEquals("test-job-single-file", jobProperties.name())
            );
        }

        @Test
        @DisplayName("Should load configuration directly from multiple temp YAML files using varargs")
        void shouldLoadConfigurationFromMultipleFiles() throws Exception {
            var baseFile = Files.createTempFile("flinkboot-base-", ".yaml");
            baseFile.toFile().deleteOnExit();
            Files.writeString(baseFile, "name: base-job\n");

            var envFile = Files.createTempFile("flinkboot-env-", ".yaml");
            envFile.toFile().deleteOnExit();
            Files.writeString(envFile, "environment:\n  execution:\n    parallelism: 4\n");

            var jobProperties = configuration(
                JobProperties.class,
                "file:" + baseFile.toAbsolutePath(),
                "file:" + envFile.toAbsolutePath()
            );

            assertAll(
                () -> assertNotNull(jobProperties),
                () -> assertEquals("base-job", jobProperties.name()),
                () -> assertTrue(jobProperties.environment().isPresent()),
                () -> assertEquals(4, jobProperties.environment().get().execution().orElseThrow().parallelism().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParametersAreNull() {
            assertAll(
                () -> assertThrows(NullPointerException.class, () -> configuration(null, "classpath:path.yaml")),
                () -> assertThrows(NullPointerException.class, () -> configuration(JobProperties.class, (String[]) null))
            );
        }
    }

}
