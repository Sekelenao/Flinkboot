package io.github.sekelenao.flinkboot.core.internal.startup;

import io.github.sekelenao.flinkboot.core.internal.parser.FusionFeatures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            var cmd = CommandLine.parse(new String[]{"-flinkboot-configurations", "custom-config.yaml"});
            var env = Map.of("FLINKBOOT_CONFIGURATIONS", "env-config.yaml");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals(List.of("custom-config.yaml"), startupEnv.configurationResourceLocations());
        }

        @Test
        @DisplayName("Should trim whitespace and filter out empty values in CommandLine option value")
        void shouldTrimWhitespaceAndFilterEmptyValues() {
            var cmd = CommandLine.parse(new String[]{"-flinkboot-configurations", "  custom-config1.yaml , , custom-config2.yaml  "});
            var resolver = new EnvVarResolver(key -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals(List.of("custom-config1.yaml", "custom-config2.yaml"), startupEnv.configurationResourceLocations());
        }

        @Test
        @DisplayName("Should return environment variable value when option is not present but env var is set")
        void shouldReturnEnvVarWhenCommandLineOptionAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var env = Map.of("FLINKBOOT_CONFIGURATIONS", "env-config.yaml");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals(List.of("env-config.yaml"), startupEnv.configurationResourceLocations());
        }

        @Test
        @DisplayName("Should return default value when both option and env var are absent")
        void shouldReturnDefaultWhenBothAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(key -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals(List.of("file:job-configuration.yaml"), startupEnv.configurationResourceLocations());
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
            var env = Map.of("KEY", "env-value");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertEquals("env-value", startupEnv.get("key").orElseThrow());
        }

        @Test
        @DisplayName("Should prefer CommandLine option over EnvVarResolver")
        void shouldPreferCommandLineOverEnv() {
            var cmd = CommandLine.parse(new String[]{"-key", "cli-value"});
            var env = Map.of("KEY", "env-value");
            var resolver = new EnvVarResolver(env::get);
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

    @Nested
    @DisplayName("Flag")
    class Flag {

        @Test
        @DisplayName("Should throw NullPointerException when flag is null")
        void shouldThrowExceptionWhenFlagIsNull() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertThrows(NullPointerException.class, () -> startupEnv.flag(null));
        }

        @Test
        @DisplayName("Should return true when flag is present in CommandLine")
        void shouldReturnTrueWhenFlagInCommandLine() {
            var cmd = CommandLine.parse(new String[]{"--my-flag"});
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertTrue(startupEnv.flag("my-flag"));
        }

        @Test
        @DisplayName("Should return true when flag is not in CommandLine but in EnvVarResolver as true")
        void shouldReturnTrueWhenFlagInEnvAsTrue() {
            var cmd = CommandLine.parse(new String[0]);
            var env = Map.of("MY_FLAG", "TrUe");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertTrue(startupEnv.flag("my-flag"));
        }

        @Test
        @DisplayName("Should return false when flag is not in CommandLine but in EnvVarResolver as false")
        void shouldReturnFalseWhenFlagInEnvAsFalse() {
            var cmd = CommandLine.parse(new String[0]);
            var env = Map.of("MY_FLAG", "false");
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertFalse(startupEnv.flag("my-flag"));
        }

        @Test
        @DisplayName("Should return false when flag is absent everywhere")
        void shouldReturnFalseWhenFlagAbsent() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            assertFalse(startupEnv.flag("my-flag"));
        }
    }

    @Nested
    @DisplayName("FusionFeatures")
    class FusionFeaturesTest {

        @Test
        @DisplayName("Should return FusionFeatures with false flags by default when absent")
        void shouldReturnFalseFlagsByDefault() {
            var cmd = CommandLine.parse(new String[0]);
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            var features = startupEnv.fusionFeatures();
            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listFusion())
            );
        }

        @Test
        @DisplayName("Should return FusionFeatures with true flags when flags are present in CommandLine")
        void shouldReturnTrueFlagsWhenInCommandLine() {
            var cmd = CommandLine.parse(new String[]{"--flinkboot-yaml-property-override", "--flinkboot-yaml-property-list-fusion"});
            var resolver = new EnvVarResolver(k -> null);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            var features = startupEnv.fusionFeatures();
            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listFusion())
            );
        }

        @Test
        @DisplayName("Should return FusionFeatures with true flags when flags are present in env variables")
        void shouldReturnTrueFlagsWhenInEnv() {
            var cmd = CommandLine.parse(new String[0]);
            var env = Map.of(
                "FLINKBOOT_YAML_PROPERTY_OVERRIDE", "true",
                "FLINKBOOT_YAML_PROPERTY_LIST_FUSION", "true"
            );
            var resolver = new EnvVarResolver(env::get);
            var startupEnv = new StartupEnvironment(cmd, resolver);
            var features = startupEnv.fusionFeatures();
            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listFusion())
            );
        }
    }
}
