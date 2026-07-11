package io.github.sekelenao.flinkboot.core.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Flinkboot")
class FlinkbootTest {

    private static final String YAML = "name: \"Flinkboot\"";

    private static final String YAML_VALUE = "Flinkboot";

    static final class TestConfig {

        @NotBlank
        private final String name;

        @JsonCreator
        public TestConfig(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

    }

    @Nested
    @DisplayName("Initialize")
    class Initialize {

        @Test
        @DisplayName("Should throw NullPointerException when args is null")
        void shouldThrowExceptionWhenArgsIsNull() {
            assertThrows(NullPointerException.class, () -> Flinkboot.initialize(null));
        }

        @Test
        @DisplayName("Should initialize successfully when args is not null")
        void shouldInitializeSuccessfully() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertNotNull(flinkboot);
        }
    }

    @Nested
    @DisplayName("LoadConfiguration")
    class LoadConfiguration {

        @Test
        @DisplayName("Should successfully load valid configuration from file")
        void shouldLoadValidConfiguration(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("config.yaml");
            Files.writeString(file, YAML);
            var args = new String[]{"-flinkboot-configuration", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);
            var config = flinkboot.configuration(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(YAML_VALUE, config.name())
            );
        }

        @Test
        @DisplayName("Should successfully load configuration using builder customizer")
        void shouldLoadConfigurationWithCustomizer(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("config.yaml");
            Files.writeString(file, YAML);
            var args = new String[]{"-flinkboot-configuration", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);
            var config = flinkboot.configuration(TestConfig.class, builder -> {});
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(YAML_VALUE, config.name())
            );
        }

        @Test
        @DisplayName("Should successfully load configuration using custom YAMLMapper")
        void shouldLoadConfigurationWithCustomMapper(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("config.yaml");
            Files.writeString(file, YAML);
            var args = new String[]{"-flinkboot-configuration", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);
            var customMapper = new YAMLMapper();
            var config = flinkboot.configuration(TestConfig.class, customMapper);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(YAML_VALUE, config.name())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            var mapper = new YAMLMapper();
            assertAll(
                () -> assertThrows(NullPointerException.class, () -> flinkboot.configuration(null)),
                () -> assertThrows(NullPointerException.class, () -> flinkboot.configuration(TestConfig.class, (Consumer<YAMLMapper.Builder>) null)),
                () -> assertThrows(NullPointerException.class, () -> flinkboot.configuration(null, builder -> {})),
                () -> assertThrows(NullPointerException.class, () -> flinkboot.configuration(TestConfig.class, (YAMLMapper) null)),
                () -> assertThrows(NullPointerException.class, () -> flinkboot.configuration(null, mapper))
            );
        }

    }

    @Nested
    @DisplayName("Parameter")
    class Parameter {

        @Test
        @DisplayName("Should throw NullPointerException when parameter name is null")
        void shouldThrowExceptionWhenParameterNameIsNull() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertThrows(NullPointerException.class, () -> flinkboot.parameter(null));
        }

        @Test
        @DisplayName("Should return empty Optional when parameter is absent")
        void shouldReturnEmptyWhenParameterAbsent() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            org.junit.jupiter.api.Assertions.assertTrue(flinkboot.parameter("any-param").isEmpty());
        }

        @Test
        @DisplayName("Should return parameter value when present in command line arguments")
        void shouldReturnParameterFromCommandLine() {
            var flinkboot = Flinkboot.initialize(new String[]{"-my-param", "my-value"});
            assertEquals("my-value", flinkboot.parameter("my-param").orElseThrow());
        }
    }
}
