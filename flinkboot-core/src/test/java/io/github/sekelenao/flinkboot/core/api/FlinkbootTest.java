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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    static final class TestMergedConfig {
        private final String name;
        private final String environment;
        private final Integer port;

        @JsonCreator
        public TestMergedConfig(
            @JsonProperty("name") String name,
            @JsonProperty("environment") String environment,
            @JsonProperty("port") Integer port
        ) {
            this.name = name;
            this.environment = environment;
            this.port = port;
        }

        public String name() { return name; }
        public String environment() { return environment; }
        public Integer port() { return port; }
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
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
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
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
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
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);
            var customMapper = new YAMLMapper();
            var config = flinkboot.configuration(TestConfig.class, customMapper);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(YAML_VALUE, config.name())
            );
        }

        @Test
        @DisplayName("Should successfully load and merge multiple configurations")
        void shouldLoadAndMergeMultipleConfigurations(@TempDir Path tempDir) throws IOException {
            var baseFile = tempDir.resolve("base.yaml");
            var overrideFile = tempDir.resolve("override.yaml");

            Files.writeString(baseFile, "name: \"BaseApp\"\nenvironment: \"dev\"\nport: 8080");
            Files.writeString(overrideFile, "environment: \"prod\"\nport: 9000");

            var args = new String[]{"-flinkboot-configurations", "file:" + baseFile.toAbsolutePath() + ",file:" + overrideFile.toAbsolutePath(), "--flinkboot-yaml-property-override"};
            var flinkboot = Flinkboot.initialize(args);
            var config = flinkboot.configuration(TestMergedConfig.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("BaseApp", config.name()),
                () -> assertEquals("prod", config.environment()),
                () -> assertEquals(9000, config.port())
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
            assertTrue(flinkboot.parameter("any-param").isEmpty());
        }

        @Test
        @DisplayName("Should return parameter value when present in command line arguments")
        void shouldReturnParameterFromCommandLine() {
            var flinkboot = Flinkboot.initialize(new String[]{"-my-param", "my-value"});
            assertEquals("my-value", flinkboot.parameter("my-param").orElseThrow());
        }
    }

    @Nested
    @DisplayName("Flag")
    class Flag {

        @Test
        @DisplayName("Should throw NullPointerException when flag name is null")
        void shouldThrowExceptionWhenFlagNameIsNull() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertThrows(NullPointerException.class, () -> flinkboot.flag(null));
        }

        @Test
        @DisplayName("Should return true when flag is present in command line arguments")
        void shouldReturnTrueWhenFlagInCommandLine() {
            var flinkboot = Flinkboot.initialize(new String[]{"--my-flag"});
            assertTrue(flinkboot.flag("my-flag"));
        }

        @Test
        @DisplayName("Should return false when flag is absent")
        void shouldReturnFalseWhenFlagAbsent() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertFalse(flinkboot.flag("my-flag"));
        }
    }
}
