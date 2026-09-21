package io.github.sekelenao.flinkboot.core.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import io.github.sekelenao.flinkboot.core.api.exception.parsing.UnresolvedPropertyPlaceholderException;
import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.flink.configuration.PipelineOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Flinkboot")
class FlinkbootTest {

    private static final String YAML = "name: \"Flinkboot\"";

    private static final String YAML_VALUE = "Flinkboot";

    private static final String MULTI_INVALID_YAML = "app-name: \"\"\n"
        + "retries: 0\n"
        + "execution:\n"
        + "  parallelism: 8\n"
        + "  max-parallelism: 4\n"
        + "state-backend:\n"
        + "  type: \"hashmap\"\n"
        + "  custom-class: \"org.example.CustomBackend\"\n";

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

    static final class TestCompositeConfig {

        @NotBlank
        private final String appName;

        @Min(1)
        private final int retries;

        @Valid
        @NotNull
        private final ExecutionProperties execution;

        @Valid
        private final StateBackendProperties stateBackend;

        @JsonCreator
        public TestCompositeConfig(
            @JsonProperty("app-name") String appName,
            @JsonProperty("retries") int retries,
            @JsonProperty("execution") ExecutionProperties execution,
            @JsonProperty("state-backend") StateBackendProperties stateBackend
        ) {
            this.appName = appName;
            this.retries = retries;
            this.execution = execution;
            this.stateBackend = stateBackend;
        }

        public String appName() {
            return appName;
        }

        public int retries() {
            return retries;
        }

        public ExecutionProperties execution() {
            return execution;
        }

        public StateBackendProperties stateBackend() {
            return stateBackend;
        }
    }

    @Nested
    @DisplayName("Initialize")
    class Initialize {

        @Test
        @DisplayName("Should throw NullPointerException when args is null")
        void shouldThrowExceptionWhenArgsIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> Flinkboot.initialize((String[]) null));
            assertEquals("args must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should initialize successfully when args is not null")
        void shouldInitializeSuccessfully() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertNotNull(flinkboot);
        }

        @Test
        @DisplayName("Should initialize cleanly without arguments using varargs")
        void shouldInitializeSuccessfullyWithoutArguments() {
            var flinkboot = Flinkboot.initialize();
            assertAll(
                () -> assertNotNull(flinkboot),
                () -> assertFalse(flinkboot.flag("unspecified-flag")),
                () -> assertTrue(flinkboot.parameter("unspecified-param").isEmpty())
            );
        }

        @Test
        @DisplayName("Should initialize successfully with multiple arguments using varargs")
        void shouldInitializeSuccessfullyWithVarargs() {
            var flinkboot = Flinkboot.initialize("-key", "value", "--flag");
            assertAll(
                () -> assertNotNull(flinkboot),
                () -> assertEquals("value", flinkboot.parameter("key").orElseThrow()),
                () -> assertTrue(flinkboot.flag("flag")),
                () -> assertFalse(flinkboot.flag("absent-flag")),
                () -> assertTrue(flinkboot.parameter("absent-key").isEmpty())
            );
        }

        @Test
        @DisplayName("Should bind and propagate arguments upon initialization")
        void shouldBindArgumentsUponInitialization() {
            var args = new String[]{"--active-flag", "-custom-param", "custom-val"};
            var flinkboot = Flinkboot.initialize(args);
            assertAll(
                () -> assertNotNull(flinkboot),
                () -> assertTrue(flinkboot.flag("active-flag")),
                () -> assertEquals("custom-val", flinkboot.parameter("custom-param").orElseThrow())
            );
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
            var customizerInvoked = new AtomicBoolean(false);
            var config = flinkboot.configuration(TestConfig.class, builder -> customizerInvoked.set(true));
            assertAll(
                () -> assertTrue(customizerInvoked.get(), "Customizer must be invoked"),
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

            var args = new String[]{"-flinkboot-configurations", "file:" + baseFile.toAbsolutePath() + ",file:" + overrideFile.toAbsolutePath(), "--flinkboot-configuration-override"};
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
        @DisplayName("Should fail fast with UnresolvedPropertyPlaceholderException when environment variable is missing")
        void shouldThrowUnresolvedPropertyPlaceholderExceptionWhenEnvVarMissing(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("config.yaml");
            Files.writeString(file, "name: \"${NON_EXISTENT_VAR_NAME}\"");
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);

            var exception = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> flinkboot.configuration(TestConfig.class));
            assertTrue(exception.getMessage().contains("NON_EXISTENT_VAR_NAME"));
        }

        @Test
        @DisplayName("Should throw ConfigurationValidationException when configuration violates validation constraints")
        void shouldThrowConfigurationValidationExceptionWhenInvalid(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("invalid-config.yaml");
            Files.writeString(file, "name: \"\"");
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);

            assertThrows(ConfigurationValidationException.class, () -> flinkboot.configuration(TestConfig.class));
        }

        @Test
        @DisplayName("Should bypass validation and load invalid configuration when disable-validation flag is enabled")
        void shouldBypassValidationWhenDisableValidationFlagProvided(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("invalid-config.yaml");
            Files.writeString(file, "name: \"\"");
            var args = new String[]{
                "-flinkboot-configurations", "file:" + file.toAbsolutePath(),
                "--flinkboot-configuration-disable-validation"
            };
            var flinkboot = Flinkboot.initialize(args);
            var config = flinkboot.configuration(TestConfig.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("", config.name())
            );
        }

        @Test
        @DisplayName("Should report all field-level and cross-field constraint violations concurrently")
        void shouldReportAllFieldAndCrossFieldViolationsConcurrently(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("multi-invalid-config.yaml");
            Files.writeString(file, MULTI_INVALID_YAML);
            var args = new String[]{"-flinkboot-configurations", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);

            var exception = assertThrows(ConfigurationValidationException.class, () -> flinkboot.configuration(TestCompositeConfig.class));
            var message = exception.getMessage();

            assertAll(
                () -> assertTrue(message.startsWith("Configuration validation failed with 4 violation(s):")),
                () -> assertTrue(message.contains("appName:")),
                () -> assertTrue(message.contains("retries:")),
                () -> assertTrue(message.contains("execution.parallelism: parallelism (8) cannot exceed max-parallelism (4)")),
                () -> assertTrue(message.contains("stateBackend.customClass: custom-class can only be specified when state backend type is CUSTOM"))
            );
        }

        @Test
        @DisplayName("Should bypass all field-level and cross-field constraint violations when disable-validation flag is enabled")
        void shouldBypassAllFieldAndCrossFieldViolationsWhenDisabled(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("multi-invalid-config.yaml");
            Files.writeString(file, MULTI_INVALID_YAML);
            var args = new String[]{
                "-flinkboot-configurations", "file:" + file.toAbsolutePath(),
                "--flinkboot-configuration-disable-validation"
            };
            var flinkboot = Flinkboot.initialize(args);
            var config = assertDoesNotThrow(() -> flinkboot.configuration(TestCompositeConfig.class));

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("", config.appName()),
                () -> assertEquals(0, config.retries()),
                () -> assertNotNull(config.execution()),
                () -> assertEquals(8, config.execution().parallelism().orElseThrow()),
                () -> assertEquals(4, config.execution().maxParallelism().orElseThrow()),
                () -> assertNotNull(config.stateBackend()),
                () -> assertEquals("org.example.CustomBackend", config.stateBackend().customClass().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            var mapper = new YAMLMapper();
            assertAll(
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> flinkboot.configuration(null));
                    assertEquals("configurationClass must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> flinkboot.configuration(TestConfig.class, (Consumer<YAMLMapper.Builder>) null));
                    assertEquals("customizer must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> flinkboot.configuration(null, builder -> {}));
                    assertEquals("configurationClass must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> flinkboot.configuration(TestConfig.class, (YAMLMapper) null));
                    assertEquals("mapper must not be null", ex.getMessage());
                },
                () -> {
                    var ex = assertThrows(NullPointerException.class, () -> flinkboot.configuration(null, mapper));
                    assertEquals("configurationClass must not be null", ex.getMessage());
                }
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
            var exception = assertThrows(NullPointerException.class, () -> flinkboot.parameter(null));
            assertEquals("parameter must not be null", exception.getMessage());
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
            var exception = assertThrows(NullPointerException.class, () -> flinkboot.flag(null));
            assertEquals("flag must not be null", exception.getMessage());
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

    @Nested
    @DisplayName("ExecutionEnvironment")
    class ExecutionEnvironment {

        @Test
        @DisplayName("Should throw NullPointerException when jobProperties is null")
        void shouldThrowExceptionWhenJobPropertiesIsNull() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            var exception = assertThrows(NullPointerException.class, () -> flinkboot.executionEnvironment(null));
            assertEquals("jobProperties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should create StreamExecutionEnvironment from valid JobProperties")
        void shouldCreateStreamExecutionEnvironmentFromJobProperties() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            var jobProps = new JobProperties("my-test-job", null);
            var env = flinkboot.executionEnvironment(jobProps);
            assertAll(
                () -> assertNotNull(env),
                () -> assertEquals("my-test-job", env.getConfiguration().get(PipelineOptions.NAME))
            );
        }
    }
}
