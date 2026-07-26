package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.RestartStrategyConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.RestartStrategyType;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.SavepointRestoreConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendType;
import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;
import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceAccessException;
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
public class EnvironmentCustomizersTest {

    @Test
    @DisplayName("Should handle empty configuration objects in all customizers cleanly")
    void shouldHandleNullConfigurationObjectsInCustomizers() {
        Configuration config = new Configuration();
        var emptyEnvConfig = new ExecutionEnvironmentConfiguration(null, null, null, null, null, null, null);

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
        var emptyPropsEnvConfig = new ExecutionEnvironmentConfiguration(null, null, null, null, null, null, Collections.emptyMap());

        new PropertiesCustomizer(config).configure(emptyPropsEnvConfig);
        assertTrue(config.keySet().isEmpty());
    }

    @Test
    @DisplayName("Should test NO_RESTART and FALLBACK restart strategy types")
    void shouldTestNoRestartAndFallbackTypes() {
        Configuration noRestartConfig = new Configuration();
        var noRestartEnvConfig = new ExecutionEnvironmentConfiguration(
            null, null,
            new RestartStrategyConfiguration(RestartStrategyType.NO_RESTART, null, null, null),
            null, null, null, null
        );
        new RestartStrategyCustomizer(noRestartConfig).configure(noRestartEnvConfig);
        assertEquals("none", noRestartConfig.get(RestartStrategyOptions.RESTART_STRATEGY));

        Configuration fallbackConfig = new Configuration();
        var fallbackEnvConfig = new ExecutionEnvironmentConfiguration(
            null, null,
            new RestartStrategyConfiguration(RestartStrategyType.FALLBACK, null, null, null),
            null, null, null, null
        );
        new RestartStrategyCustomizer(fallbackConfig).configure(fallbackEnvConfig);
        assertNull(fallbackConfig.get(RestartStrategyOptions.RESTART_STRATEGY));
    }

    @Test
    @DisplayName("Should test StateBackendConfiguration optional fields")
    void shouldTestStateBackendOptionalFields() {
        Configuration config = new Configuration();
        var stateBackendConfig = new StateBackendConfiguration(
            StateBackendType.HASHMAP,
            CheckpointStorageType.JOBMANAGER,
            null,
            false,
            false,
            null
        );
        var envConfig = new ExecutionEnvironmentConfiguration(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envConfig);

        assertAll(
            () -> assertEquals("hashmap", config.get(StateBackendOptions.STATE_BACKEND)),
            () -> assertEquals("jobmanager", config.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
            () -> assertNull(config.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
            () -> assertFalse(config.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS))
        );
    }

    @Test
    @DisplayName("Should test SavepointRestoreConfiguration optional fields")
    void shouldTestSavepointRestoreOptionalFields() {
        Configuration config = new Configuration();
        var savepointConfig = new SavepointRestoreConfiguration(
            "/tmp/savepoint-2",
            null,
            null
        );
        var envConfig = new ExecutionEnvironmentConfiguration(null, null, null, null, savepointConfig, null, null);

        new SavepointRestoreCustomizer(config).configure(envConfig);

        assertAll(
            () -> assertEquals("/tmp/savepoint-2", config.get(StateRecoveryOptions.SAVEPOINT_PATH)),
            () -> assertFalse(config.get(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
            () -> assertNotNull(config.get(StateRecoveryOptions.RESTORE_MODE))
        );
    }

    @Test
    @DisplayName("Should test CheckpointingConfiguration disabled and optional fields")
    void shouldTestCheckpointingOptionalFields() {
        Configuration config = new Configuration();
        var chkConfig = new CheckpointingConfiguration(
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
        var envConfig = new ExecutionEnvironmentConfiguration(null, chkConfig, null, null, null, null, null);

        new CheckpointingCustomizer(config).configure(envConfig);

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
