package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateLatencyTrackOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendCustomizer Tests")
class StateBackendCustomizerTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should successfully instantiate when Configuration is non-null")
        void shouldSuccessfullyInstantiateWithValidConfiguration() {
            assertDoesNotThrow(() -> new StateBackendCustomizer(new Configuration()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when Configuration is null")
        void shouldThrowNullPointerExceptionWhenConfigurationIsNull() {
            assertThrows(NullPointerException.class, () -> new StateBackendCustomizer(null));
        }
    }

    @Nested
    @DisplayName("Configure")
    class Configure {

        @Test
        @DisplayName("Should throw NullPointerException when ExecutionEnvironmentProperties is null")
        void shouldThrowNullPointerExceptionWhenEnvironmentPropertiesIsNull() {
            var customizer = new StateBackendCustomizer(new Configuration());
            assertThrows(NullPointerException.class, () -> customizer.configure(null));
        }

        @Test
        @DisplayName("Should leave configuration unmodified when state backend configuration is empty")
        void shouldLeaveConfigurationUnmodifiedWhenStateBackendEmpty() {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);

            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "Expected configuration to remain untouched when state-backend is absent");
        }

        @Test
        @DisplayName("Should leave configuration unmodified when state backend properties are all null")
        void shouldLeaveConfigurationUnmodifiedWhenStateBackendPropertiesAllNull() {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(null, null, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "Expected configuration to remain untouched when all state backend properties are null");
        }

        @ParameterizedTest(name = "Should map backend type {0} to \"{1}\"")
        @CsvSource({
            "HASHMAP, hashmap",
            "ROCKSDB, rocksdb",
            "CHANGELOG, changelog"
        })
        @DisplayName("Should configure standard state backend types")
        void shouldConfigureStandardStateBackendTypes(StateBackendType type, String expectedBackend) {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(type, null, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals(expectedBackend, flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should configure custom state backend class name when type is CUSTOM")
        void shouldConfigureCustomStateBackend() {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(
                StateBackendType.CUSTOM,
                null,
                null,
                null,
                "org.example.MyCustomStateBackendFactory"
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("org.example.MyCustomStateBackendFactory", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @ParameterizedTest(name = "Should map checkpoint storage {0} to \"{1}\"")
        @CsvSource({
            "JOBMANAGER, jobmanager",
            "FILESYSTEM, filesystem"
        })
        @DisplayName("Should configure checkpoint storage types")
        void shouldConfigureCheckpointStorageTypes(CheckpointStorageType storageType, String expectedStorage) {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(null, storageType, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals(expectedStorage, flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @ParameterizedTest(name = "Should configure incremental checkpoints to {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Should configure incremental checkpoints")
        void shouldConfigureIncrementalCheckpoints(boolean incremental) {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(null, null, incremental, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals(incremental, flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @ParameterizedTest(name = "Should configure latency tracking enabled to {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Should configure state access latency tracking")
        void shouldConfigureLatencyTracking(boolean latencyTracking) {
            var flinkConfig = new Configuration();
            var customizer = new StateBackendCustomizer(flinkConfig);
            var stateBackendConfig = new StateBackendProperties(null, null, null, latencyTracking, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals(latencyTracking, flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should apply all state backend properties when fully configured")
        void shouldApplyAllStateBackendProperties() {
            var flinkConfig = new Configuration();
            var stateBackendConfig = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                false,
                true,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("rocksdb", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals("filesystem", flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertFalse(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertTrue(flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED)),
                () -> assertEquals(4, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should configure custom state backend with storage and asymmetric feature flags")
        void shouldConfigureCustomStateBackendWithStorageAndAsymmetricFlags() {
            var flinkConfig = new Configuration();
            var stateBackendConfig = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                "com.example.CustomBackendFactory"
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("com.example.CustomBackendFactory", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals("filesystem", flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertFalse(flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED)),
                () -> assertEquals(4, flinkConfig.toMap().size())
            );
        }

        @ParameterizedTest(name = "Should preserve CHECKPOINTS_DIRECTORY when configuring backend {0} with storage {1}")
        @CsvSource({
            "HASHMAP, JOBMANAGER",
            "ROCKSDB, FILESYSTEM",
            "CHANGELOG, FILESYSTEM"
        })
        @DisplayName("Should preserve existing CHECKPOINTS_DIRECTORY without interference")
        void shouldPreserveExistingCheckpointsDirectory(StateBackendType type, CheckpointStorageType storageType) {
            var flinkConfig = new Configuration();
            flinkConfig.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "file:///preconfigured-checkpoints");

            var stateBackendConfig = new StateBackendProperties(type, storageType, true, true, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("file:///preconfigured-checkpoints", flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
                () -> assertEquals(storageType.toString().toLowerCase(), flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertEquals(type.name().toLowerCase(), flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertTrue(flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED))
            );
        }

        @Test
        @DisplayName("Should not set CHECKPOINTS_DIRECTORY when none was previously configured")
        void shouldNotSetCheckpointsDirectoryWhenAbsent() {
            var flinkConfig = new Configuration();
            var stateBackendConfig = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                true,
                true,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("rocksdb", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals("filesystem", flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertTrue(flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED)),
                () -> assertNull(flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
                () -> assertEquals(4, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should overwrite existing STATE_BACKEND when new type is provided")
        void shouldOverwriteExistingStateBackend() {
            var flinkConfig = new Configuration();
            flinkConfig.set(StateBackendOptions.STATE_BACKEND, "rocksdb");

            var stateBackendConfig = new StateBackendProperties(StateBackendType.HASHMAP, null, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("hashmap", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should preserve existing STATE_BACKEND when backend type is not specified")
        void shouldPreserveExistingStateBackendWhenTypeNotSpecified() {
            var flinkConfig = new Configuration();
            flinkConfig.set(StateBackendOptions.STATE_BACKEND, "rocksdb");

            var stateBackendConfig = new StateBackendProperties(null, null, true, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals("rocksdb", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertEquals(2, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should preserve other pre-existing configuration options")
        void shouldPreservePreExistingConfigurationOptions() {
            var flinkConfig = new Configuration();
            flinkConfig.set(CoreOptions.DEFAULT_PARALLELISM, 4);

            var stateBackendConfig = new StateBackendProperties(StateBackendType.HASHMAP, null, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

            new StateBackendCustomizer(flinkConfig).configure(envProps);

            assertAll(
                () -> assertEquals(4, flinkConfig.get(CoreOptions.DEFAULT_PARALLELISM)),
                () -> assertEquals("hashmap", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals(2, flinkConfig.toMap().size())
            );
        }
    }
}
