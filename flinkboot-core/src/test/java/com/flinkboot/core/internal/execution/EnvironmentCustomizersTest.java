package com.flinkboot.core.internal.execution;

import com.flinkboot.core.api.exception.FlinkbootException;
import com.flinkboot.core.api.exception.resource.ResourceAccessException;
import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.api.properties.state.CheckpointStorageType;
import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
import com.flinkboot.core.internal.execution.customizer.CheckpointingCustomizer;
import com.flinkboot.core.internal.execution.customizer.ExecutionCustomizer;
import com.flinkboot.core.internal.execution.customizer.LocalWebUiCustomizer;
import com.flinkboot.core.internal.execution.customizer.PropertiesCustomizer;
import com.flinkboot.core.internal.execution.customizer.RestartStrategyCustomizer;
import com.flinkboot.core.internal.execution.customizer.SavepointRestoreCustomizer;
import com.flinkboot.core.internal.execution.customizer.StateBackendCustomizer;
import com.flinkboot.core.api.properties.execution.ExecutionProperties;
import com.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateLatencyTrackOptions;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.time.Duration;

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
            false,
            false,
            null
        , null);
        var envProps = new ExecutionEnvironmentProperties(null, null, null, stateBackendConfig, null, null, null);

        new StateBackendCustomizer(config).configure(envProps);

        assertAll(
            () -> assertEquals("hashmap", config.get(StateBackendOptions.STATE_BACKEND)),
            () -> assertEquals("jobmanager", config.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
            () -> assertNull(config.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
            () -> assertFalse(config.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
            () -> assertFalse(config.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED))
        );
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

    @Test
    @DisplayName("Should disable buffer timeout when configured to -1 millisecond")
    void shouldDisableBufferTimeoutWhenConfiguredToNegativeOneMillisecond() {
        Configuration config = new Configuration();

        var executionConfig = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                null,
                null,
                Duration.ofMillis(-1),
                null,
                null
        );

        var envConfig = new ExecutionEnvironmentProperties(
                executionConfig,
                null,
                null,
                null,
                null,
                null,
                null
        );

        new ExecutionCustomizer(config).configure(envConfig);

        assertAll(
                () -> assertFalse(config.get(ExecutionOptions.BUFFER_TIMEOUT_ENABLED)),
                () -> assertFalse(config.contains(ExecutionOptions.BUFFER_TIMEOUT))
        );
    }

    @Test
    @DisplayName("Should apply buffer timeout when configured to a positive duration")
    void shouldApplyPositiveBufferTimeout() {
        Configuration config = new Configuration();
        Duration bufferTimeout = Duration.ofMillis(100);

        var executionConfig = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                null,
                null,
                bufferTimeout,
                null,
                null
        );

        var envConfig = new ExecutionEnvironmentProperties(
                executionConfig,
                null,
                null,
                null,
                null,
                null,
                null
        );

        new ExecutionCustomizer(config).configure(envConfig);

        assertAll(
                () -> assertEquals(bufferTimeout, config.get(ExecutionOptions.BUFFER_TIMEOUT)),
                () -> assertTrue(config.contains(ExecutionOptions.BUFFER_TIMEOUT))
        );
    }
}
