package io.github.sekelenao.flinkboot.test.api;

import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static io.github.sekelenao.flinkboot.test.api.FlinkbootTest.configuration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlinkbootTest API")
class FlinkbootTestTest {


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
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> configuration(null, "classpath:path.yaml"));
                    assertEquals("configurationClass must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> configuration(JobProperties.class, (String[]) null));
                    assertEquals("paths must not be null", ex.getMessage());
                }
            );
        }
    }
}
