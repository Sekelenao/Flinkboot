package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.ExternalizedCheckpointCleanupMode;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import io.github.sekelenao.flinkboot.core.api.properties.local.LocalWebUiProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FailureRateRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyType;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.RestoreMode;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateLatencyTrackOptions;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExecutionEnvironmentFactory Tests")
class ExecutionEnvironmentFactoryTest {

    @Nested
    @DisplayName("Configuration Mapping Tests")
    class ConfigurationMappingTests {

        @Test
        @DisplayName("Should throw NullPointerException when jobProperties is null")
        void shouldThrowNpeWhenJobPropertiesIsNull() {
            var factory = new ExecutionEnvironmentFactory();
            assertThrows(NullPointerException.class, () -> factory.create(null));
        }

        @Test
        @DisplayName("Should correctly map JobProperties into Flink Configuration")
        void shouldMapJobPropertiesToFlinkConfiguration() {
            var execConfig = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                8,
                128,
                Duration.ofMillis(100),
                Duration.ofMillis(200),
                true
            );
            var envProps = new ExecutionEnvironmentProperties(execConfig, null, null, null, null, null, null);
            var jobConfig = new JobProperties("my-test-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("my-test-job", flinkConfig.get(PipelineOptions.NAME)),
                () -> assertEquals(RuntimeExecutionMode.STREAMING, flinkConfig.get(ExecutionOptions.RUNTIME_MODE)),
                () -> assertEquals(8, flinkConfig.get(CoreOptions.DEFAULT_PARALLELISM)),
                () -> assertEquals(128, flinkConfig.get(PipelineOptions.MAX_PARALLELISM)),
                () -> assertEquals(Duration.ofMillis(100), flinkConfig.get(ExecutionOptions.BUFFER_TIMEOUT)),
                () -> assertEquals(Duration.ofMillis(200), flinkConfig.get(PipelineOptions.AUTO_WATERMARK_INTERVAL)),
                () -> assertTrue(flinkConfig.get(PipelineOptions.OBJECT_REUSE))
            );
        }

        @Test
        @DisplayName("Should correctly map CheckpointingProperties into Flink Configuration")
        void shouldMapCheckpointingPropertiesToFlinkConfiguration() {
            var chkConfig = new CheckpointingProperties(
                true,
                Duration.ofMillis(10000),
                CheckpointingMode.EXACTLY_ONCE,
                Duration.ofMillis(60000),
                Duration.ofMillis(5000),
                2,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
                true,
                Duration.ofMillis(1000),
                "s3://my-bucket/checkpoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, chkConfig, null, null, null, null, null);
            var jobConfig = new JobProperties("checkpoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals(Duration.ofMillis(10000), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
                () -> assertEquals(org.apache.flink.core.execution.CheckpointingMode.EXACTLY_ONCE, flinkConfig.get(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE)),
                () -> assertEquals(Duration.ofMillis(60000), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_TIMEOUT)),
                () -> assertEquals(Duration.ofMillis(5000), flinkConfig.get(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS)),
                () -> assertEquals(2, flinkConfig.get(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS)),
                () -> assertEquals(ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION, flinkConfig.get(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.ENABLE_UNALIGNED)),
                () -> assertEquals(Duration.ofMillis(1000), flinkConfig.get(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT)),
                () -> assertEquals("s3://my-bucket/checkpoints", flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY))
            );
        }

        @Test
        @DisplayName("Should correctly map FixedDelay RestartStrategyProperties into Flink Configuration")
        void shouldMapFixedDelayRestartStrategyToFlinkConfiguration() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofMillis(5000));
            var restartConfig = new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartConfig, null, null, null, null);
            var jobConfig = new JobProperties("restart-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("fixed-delay", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(3, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS)),
                () -> assertEquals(Duration.ofMillis(5000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY))
            );
        }

        @Test
        @DisplayName("Should correctly map FailureRate RestartStrategyProperties into Flink Configuration")
        void shouldMapFailureRateRestartStrategyToFlinkConfiguration() {
            var failure = new FailureRateRestartProperties(3, Duration.ofMillis(60000), Duration.ofMillis(1000));
            var restartConfig = new RestartStrategyProperties(RestartStrategyType.FAILURE_RATE, null, failure, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartConfig, null, null, null, null);
            var jobConfig = new JobProperties("restart-failure-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("failure-rate", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(3, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL)),
                () -> assertEquals(Duration.ofMillis(60000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL)),
                () -> assertEquals(Duration.ofMillis(1000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY))
            );
        }

        @Test
        @DisplayName("Should correctly map ExponentialDelay RestartStrategyProperties into Flink Configuration")
        void shouldMapExponentialDelayRestartStrategyToFlinkConfiguration() {
            var expo = new ExponentialDelayRestartProperties(Duration.ofMillis(1000), Duration.ofMillis(60000), 2.0, Duration.ofMillis(3600000), 0.1);
            var restartConfig = new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartConfig, null, null, null, null);
            var jobConfig = new JobProperties("restart-expo-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("exponential-delay", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(Duration.ofMillis(1000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF)),
                () -> assertEquals(Duration.ofMillis(60000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF)),
                () -> assertEquals(2.0, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER)),
                () -> assertEquals(Duration.ofMillis(3600000), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD)),
                () -> assertEquals(0.1, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR))
            );
        }


        @Test
        @DisplayName("Should correctly map NoRestart RestartStrategyProperties into Flink Configuration")
        void shouldMapNoRestartToFlinkConfiguration() {
            var restartConfig = new RestartStrategyProperties(RestartStrategyType.NO_RESTART, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartConfig, null, null, null, null);
            var jobConfig = new JobProperties("no-restart-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertEquals("none", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY));
        }

        @Test
        @DisplayName("Should correctly map StateBackendProperties into Flink Configuration")
        void shouldMapStateBackendPropertiesToFlinkConfiguration() {
            var stateConfig = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                "s3://my-bucket/checkpoints",
                true,
                true,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, stateConfig, null, null, null);
            var jobConfig = new JobProperties("state-backend-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("rocksdb", flinkConfig.get(StateBackendOptions.STATE_BACKEND)),
                () -> assertEquals("filesystem", flinkConfig.get(CheckpointingOptions.CHECKPOINT_STORAGE)),
                () -> assertEquals("s3://my-bucket/checkpoints", flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.INCREMENTAL_CHECKPOINTS)),
                () -> assertTrue(flinkConfig.get(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED))
            );
        }

        @Test
        @DisplayName("Should correctly map SavepointRestoreProperties into Flink Configuration")
        void shouldMapSavepointRestorePropertiesToFlinkConfiguration() {
            var savepointConfig = new SavepointRestoreProperties(
                "/tmp/savepoint-1",
                true,
                RestoreMode.CLAIM
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);
            var jobConfig = new JobProperties("savepoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals("/tmp/savepoint-1", flinkConfig.get(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertTrue(flinkConfig.get(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertEquals("CLAIM", flinkConfig.get(StateRecoveryOptions.RESTORE_MODE).toString())
            );
        }

        @Test
        @DisplayName("Should correctly map LocalWebUiProperties into Flink Configuration and create LocalStreamEnvironment")
        void shouldMapLocalWebUiPropertiesToFlinkConfiguration() {
            var localWebUiConfig = new LocalWebUiProperties(true, 8081, "127.0.0.1");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, localWebUiConfig, null);
            var jobConfig = new JobProperties("local-webui-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            StreamExecutionEnvironment env = factory.create(jobConfig);

            assertAll(
                () -> assertNotNull(env),
                () -> assertTrue(env instanceof LocalStreamEnvironment),
                () -> assertEquals(8081, env.getConfiguration().get(RestOptions.PORT)),
                () -> assertEquals("127.0.0.1", env.getConfiguration().get(RestOptions.BIND_ADDRESS))
            );
        }

        @Test
        @DisplayName("Should not configure local WebUI options when enabled is false")
        void shouldNotConfigureLocalWebUiOptionsWhenDisabled() {
            var localWebUiConfig = new LocalWebUiProperties(false, 9090, "0.0.0.0");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, localWebUiConfig, null);
            var jobConfig = new JobProperties("disabled-webui-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = env.getConfiguration();

            assertAll(
                () -> assertEquals(RestOptions.PORT.defaultValue(), flinkConfig.get(RestOptions.PORT)),
                () -> assertEquals(RestOptions.BIND_ADDRESS.defaultValue(), flinkConfig.get(RestOptions.BIND_ADDRESS))
            );
        }

        @Test
        @DisplayName("Should throw InvalidLocalWebUiPropertiesException when localWebUi is enabled on a cluster environment")
        void shouldThrowExceptionWhenLocalWebUiEnabledOnClusterEnvironment() {
            var localWebUiConfig = new LocalWebUiProperties(true, 8081, "127.0.0.1");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, localWebUiConfig, null);
            var jobConfig = new JobProperties("local-webui-cluster-job", envProps);

            var mockClusterEnv = org.mockito.Mockito.mock(StreamExecutionEnvironment.class);
            try (var mockedStatic = org.mockito.Mockito.mockStatic(StreamExecutionEnvironment.class)) {
                mockedStatic.when(StreamExecutionEnvironment::getExecutionEnvironment).thenReturn(mockClusterEnv);

                var factory = new ExecutionEnvironmentFactory();
                assertThrows(io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidLocalWebUiPropertiesException.class,
                    () -> factory.create(jobConfig));
            }
        }

        @Test
        @DisplayName("Should correctly map custom properties into Flink Configuration")
        void shouldMapPropertiesToFlinkConfiguration() {
            var props = Map.of("taskmanager.memory.managed.fraction", "0.4", "pipeline.operator-chaining.enabled", "true");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, props);
            var jobConfig = new JobProperties("properties-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);
            var flinkConfig = (Configuration) env.getConfiguration();

            assertAll(
                () -> assertEquals("0.4", flinkConfig.getString("taskmanager.memory.managed.fraction", null)),
                () -> assertEquals("true", flinkConfig.getString("pipeline.operator-chaining.enabled", null))
            );
        }

        @Test
        @DisplayName("Should return StreamExecutionEnvironment holding the configured parameters")
        void shouldReturnStreamExecutionEnvironmentWithConfiguredParameters() {
            var execConfig = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                4,
                32,
                Duration.ofMillis(50),
                Duration.ofMillis(150),
                true
            );
            var envProps = new ExecutionEnvironmentProperties(execConfig, null, null, null, null, null, null);
            var jobConfig = new JobProperties("environment-test-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            StreamExecutionEnvironment env = factory.create(jobConfig);

            assertAll(
                () -> assertEquals("environment-test-job", env.getConfiguration().get(PipelineOptions.NAME)),
                () -> assertEquals(RuntimeExecutionMode.STREAMING, env.getConfiguration().get(ExecutionOptions.RUNTIME_MODE)),
                () -> assertEquals(4, env.getParallelism()),
                () -> assertEquals(32, env.getMaxParallelism()),
                () -> assertEquals(50L, env.getBufferTimeout()),
                () -> assertEquals(Duration.ofMillis(150), env.getConfiguration().get(PipelineOptions.AUTO_WATERMARK_INTERVAL)),
                () -> assertTrue(env.getConfig().isObjectReuseEnabled())
            );
        }
    }
}

