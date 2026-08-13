package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;
import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceAccessException;
import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyType;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.CheckpointingCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.ExecutionCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.LocalWebUiCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.PropertiesCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.RestartStrategyCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.SavepointRestoreCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.StateBackendCustomizer;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Environment Customizers Tests")
class EnvironmentCustomizersTest {

    @Test
    @DisplayName("Should handle empty configuration objects in all customizers cleanly")
    void shouldHandleNullConfigurationObjectsInCustomizers() {
        Configuration config = new Configuration();
        var emptyEnvConfig = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);

        assertAll(
            () -> new ExecutionCustomizer(config).configure(emptyEnvConfig),
            () -> new CheckpointingCustomizer(config).configure(emptyEnvConfig),
            () -> new RestartStrategyCustomizer(config).configure(emptyEnvConfig),
            () -> new StateBackendCustomizer(config).configure(emptyEnvConfig),
            () -> new SavepointRestoreCustomizer(config).configure(emptyEnvConfig),
            () -> new LocalWebUiCustomizer(config).configure(emptyEnvConfig),
            () -> new PropertiesCustomizer(config).configure(emptyEnvConfig)
        );
    }

    @Test
    @DisplayName("Should handle empty properties map in PropertiesCustomizer")
    void shouldHandleEmptyPropertiesMap() {
        Configuration config = new Configuration();
        var emptyPropsEnvConfig = new ExecutionEnvironmentProperties(null, null, null, null, null, null, Collections.emptyMap());

        new PropertiesCustomizer(config).configure(emptyPropsEnvConfig);
        assertTrue(config.keySet().isEmpty());
    }

    @Test
    @DisplayName("Should test NO_RESTART and FALLBACK restart strategy types")
    void shouldTestNoRestartAndFallbackTypes() {
        Configuration noRestartConfig = new Configuration();
        var noRestartEnvConfig = new ExecutionEnvironmentProperties(
            null, null,
            new RestartStrategyProperties(RestartStrategyType.NO_RESTART, null, null, null),
            null, null, null, null
        );
        new RestartStrategyCustomizer(noRestartConfig).configure(noRestartEnvConfig);
        assertEquals("none", noRestartConfig.get(RestartStrategyOptions.RESTART_STRATEGY));

        Configuration fallbackConfig = new Configuration();
        var fallbackEnvConfig = new ExecutionEnvironmentProperties(
            null, null,
            new RestartStrategyProperties(RestartStrategyType.FALLBACK, null, null, null),
            null, null, null, null
        );
        new RestartStrategyCustomizer(fallbackConfig).configure(fallbackEnvConfig);
        assertNull(fallbackConfig.get(RestartStrategyOptions.RESTART_STRATEGY));
    }

    @Test
    @DisplayName("Should test StateBackendProperties optional fields with HASHMAP")
    void shouldTestStateBackendOptionalFields() {
        Configuration config = new Configuration();
        var stateBackendConfig = new StateBackendProperties(
            StateBackendType.HASHMAP,
            CheckpointStorageType.JOBMANAGER,
            null,
            false,
            false,
            null
        );
        var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envProps);

        assertAll(
            () -> assertEquals("hashmap", config.get(StateBackendOptions.STATE_BACKEND)),
            () -> assertEquals("jobmanager", config.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
            () -> assertNull(config.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
            () -> assertFalse(config.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS))
        );
    }

    @Test
    @DisplayName("Should configure ROCKSDB state backend with incremental checkpoints and filesystem storage")
    void shouldConfigureRocksDbStateBackend() {
        Configuration config = new Configuration();
        var stateBackendConfig = new StateBackendProperties(
            StateBackendType.ROCKSDB,
            CheckpointStorageType.FILESYSTEM,
            "file:///tmp/checkpoints",
            true,
            true,
            null
        );
        var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envProps);

        assertAll(
            () -> assertEquals("rocksdb", config.get(StateBackendOptions.STATE_BACKEND)),
            () -> assertEquals("filesystem", config.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
            () -> assertEquals("file:///tmp/checkpoints", config.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
            () -> assertTrue(config.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS))
        );
    }

    @Test
    @DisplayName("Should configure CHANGELOG state backend")
    void shouldConfigureChangelogStateBackend() {
        Configuration config = new Configuration();
        var stateBackendConfig = new StateBackendProperties(
            StateBackendType.CHANGELOG,
            null,
            null,
            null,
            null,
            null
        );
        var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envProps);

        assertEquals("changelog", config.get(StateBackendOptions.STATE_BACKEND));
    }

    @Test
    @DisplayName("Should configure CUSTOM state backend with custom class name")
    void shouldConfigureCustomStateBackend() {
        Configuration config = new Configuration();
        var stateBackendConfig = new StateBackendProperties(
            StateBackendType.CUSTOM,
            null,
            null,
            null,
            null,
            "org.example.MyCustomStateBackendFactory"
        );
        var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envProps);

        assertEquals("org.example.MyCustomStateBackendFactory", config.get(StateBackendOptions.STATE_BACKEND));
    }

    @Test
    @DisplayName("Should test SavepointRestoreProperties optional fields")
    void shouldTestSavepointRestoreOptionalFields() {
        Configuration config = new Configuration();
        var savepointConfig = new SavepointRestoreProperties(
            "/tmp/savepoint-2",
            null,
            null
        );
        var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

        new SavepointRestoreCustomizer(config).configure(envProps);

        assertAll(
            () -> assertEquals("/tmp/savepoint-2", config.get(StateRecoveryOptions.SAVEPOINT_PATH)),
            () -> assertFalse(config.get(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
            () -> assertNotNull(config.get(StateRecoveryOptions.RESTORE_MODE))
        );
    }

    @Test
    @DisplayName("Should test CheckpointingProperties disabled and optional fields")
    void shouldTestCheckpointingOptionalFields() {
        Configuration config = new Configuration();
        var chkConfig = new CheckpointingProperties(
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null
        );
        var envProps = new ExecutionEnvironmentProperties(null, chkConfig, null, null, null, null, null);

        new CheckpointingCustomizer(config).configure(envProps);

        assertAll(
            () -> assertNull(config.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
            () -> assertNull(config.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY))
        );
    }

    @Test
    @DisplayName("Should test Exception constructors")
    void shouldTestExceptionConstructors() {
        Throwable cause = new RuntimeException("root cause");
        ResourceAccessException rae = new ResourceAccessException("resource error", cause);
        FlinkbootException fe = new FlinkbootException("flinkboot error", cause);

        assertAll(
            () -> assertTrue(rae.getMessage().contains("resource error")),
            () -> assertEquals(cause, rae.getCause()),
            () -> assertEquals("flinkboot error", fe.getMessage()),
            () -> assertEquals(cause, fe.getCause())
        );
    }
}
