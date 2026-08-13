package io.github.sekelenao.flinkboot.test.api;

import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.typing.collection.ListTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.collection.MapTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static io.github.sekelenao.flinkboot.test.api.FlinkbootTest.assertPojo;
import static io.github.sekelenao.flinkboot.test.api.FlinkbootTest.configuration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlinkbootTest API")
class FlinkbootTestTest {

    public static class SamplePojo {
        public String name;
        public int value;

        @TypeInfo(LocalDateTimeTypeInfoFactory.class)
        public LocalDateTime timestamp;

        @TypeInfo(LocalDateTypeInfoFactory.class)
        public LocalDate date;

        @TypeInfo(LocalTimeTypeInfoFactory.class)
        public LocalTime time;

        @TypeInfo(DurationTypeInfoFactory.class)
        public Duration duration;

        @TypeInfo(ListTypeInfoFactory.class)
        public List<String> list;

        @TypeInfo(MapTypeInfoFactory.class)
        public Map<String, Integer> map;
    }

    public static class InvalidPojo {
        private String name;
    }

    @Nested
    @DisplayName("assertPojo")
    class AssertPojoTests {

        @Test
        @DisplayName("Should pass assertPojo for a valid POJO with @TypeInfo annotations")
        void shouldPassAssertPojo() {
            assertDoesNotThrow(() -> assertPojo(SamplePojo.class));
        }

        @Test
        @DisplayName("Should fail assertPojo for an invalid POJO structure")
        void shouldFailAssertPojoForInvalidClass() {
            assertThrows(AssertionFailedError.class, () -> assertPojo(InvalidPojo.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when class is null")
        void shouldThrowExceptionWhenClassIsNull() {
            assertThrows(NullPointerException.class, () -> assertPojo(null));
        }
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
