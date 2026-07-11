package io.github.sekelenao.internal.startup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StartupEnvironment")
class StartupEnvironmentTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when args array is null")
        void shouldThrowExceptionWhenArgsIsNull() {
            assertThrows(NullPointerException.class, () -> new StartupEnvironment(null));
        }

        @Test
        @DisplayName("Should throw NullPointerException when CommandLine is null in testing constructor")
        void shouldThrowExceptionWhenCommandLineIsNull() {
            var resolver = new EnvVarResolver(System::getenv);
            assertThrows(NullPointerException.class, () -> new StartupEnvironment(null, resolver));
        }

        @Test
        @DisplayName("Should throw NullPointerException when EnvVarResolver is null in testing constructor")
        void shouldThrowExceptionWhenEnvVarResolverIsNull() {
            var cmd = CommandLine.parse(new String[0]);
            assertThrows(NullPointerException.class, () -> new StartupEnvironment(cmd, null));
        }
    }

    @Nested
    @DisplayName("ConfigurationResourceLocation")
    class ConfigurationResourceLocation {

        @Test
        @DisplayName("Should return CommandLine option value when option is present")
        void shouldReturnCommandLineOptionWhenPresent() {
            var cmd = CommandLine.parse(new String[]{"-flinkboot-configuration", "custom-config.yaml"});
            var env = Map.of("FLINKBOOT_CONFIGURATION", "env-config.yaml");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("custom-config.yaml", startupEnv.configurationResourceLocation());
        }

        @Test
        @DisplayName("Should return environment variable value when option is not present but env var is set")
        void shouldReturnEnvVarWhenCommandLineOptionAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var env = Map.of("FLINKBOOT_CONFIGURATION", "env-config.yaml");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("env-config.yaml", startupEnv.configurationResourceLocation());
        }

        @Test
        @DisplayName("Should return default value when both option and env var are absent")
        void shouldReturnDefaultWhenBothAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(key -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("file:job-configuration.yaml", startupEnv.configurationResourceLocation());
        }
    }

    @Nested
    @DisplayName("Get")
    class Get {

        @Test
        @DisplayName("Should return parameter value from CommandLine when present")
        void shouldReturnFromCommandLine() {
            var cmd = CommandLine.parse(new String[]{"-key", "value"});
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("value", startupEnv.get("key").orElseThrow());
        }

        @Test
        @DisplayName("Should return parameter value from EnvVarResolver when not in CommandLine but in env")
        void shouldReturnFromEnvVar() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(k -> "env-value");
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("env-value", startupEnv.get("key").orElseThrow());
        }

        @Test
        @DisplayName("Should prefer CommandLine option over EnvVarResolver")
        void shouldPreferCommandLineOverEnv() {
            var cmd = CommandLine.parse(new String[]{"-key", "cli-value"});
            var resolver = new EnvVarResolver(k -> "env-value");
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("cli-value", startupEnv.get("key").orElseThrow());
        }

        @Test
        @DisplayName("Should return empty Optional when key is absent everywhere")
        void shouldReturnEmptyWhenAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertTrue(startupEnv.get("key").isEmpty());
        }
    }
}
