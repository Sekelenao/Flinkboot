package com.flinkboot.core.internal.execution;

import com.flinkboot.core.api.exception.execution.UnsupportedExecutionEnvironmentException;
import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.JobProperties;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.api.properties.execution.ExecutionProperties;
import com.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import com.flinkboot.core.api.properties.local.LocalWebUiProperties;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
        @DisplayName("Should disable checkpointing on environment when checkpointing.enabled is false")
        void shouldDisableCheckpointingWhenEnabledIsFalse() {
            var chkConfig = new CheckpointingProperties(
                false, null, null, null, null, null, null, null, null, null, null, null
            );
            var props = Map.of("execution.checkpointing.interval", "5000");
            var envProps = new ExecutionEnvironmentProperties(null, chkConfig, null, null, null, null, props);
            var jobConfig = new JobProperties("disabled-checkpoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);

            assertFalse(env.getCheckpointConfig().isCheckpointingEnabled());
        }

        @Test
        @DisplayName("Should preserve checkpointing when checkpointing is absent")
        void shouldPreserveCheckpointingWhenCheckpointingIsAbsent() {
            var props = Map.of("execution.checkpointing.interval", "5000");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, props);
            var jobConfig = new JobProperties("absent-checkpoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);

            assertTrue(env.getCheckpointConfig().isCheckpointingEnabled());
        }

        @Test
        @DisplayName("Should preserve checkpointing when checkpointing.enabled is omitted")
        void shouldPreserveCheckpointingWhenEnabledIsOmitted() {
            var chkConfig = new CheckpointingProperties(
                null, Duration.ofMillis(5000), null, null, null, null, null, null, null, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, chkConfig, null, null, null, null, null);
            var jobConfig = new JobProperties("omitted-enabled-checkpoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);

            assertTrue(env.getCheckpointConfig().isCheckpointingEnabled());
        }

        @Test
        @DisplayName("Should preserve checkpointing when checkpointing.enabled is true")
        void shouldPreserveCheckpointingWhenEnabledIsTrue() {
            var chkConfig = new CheckpointingProperties(
                true, Duration.ofMillis(5000), null, null, null, null, null, null, null, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, chkConfig, null, null, null, null, null);
            var jobConfig = new JobProperties("explicit-enabled-checkpoint-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            var env = factory.create(jobConfig);

            assertTrue(env.getCheckpointConfig().isCheckpointingEnabled());
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
        @DisplayName("Should configure local WebUI and create LocalStreamEnvironment when enabled is omitted")
        void shouldConfigureLocalWebUiWhenEnabledIsOmitted() {
            var localWebUiConfig = new LocalWebUiProperties(null, 8082, "127.0.0.1");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, localWebUiConfig, null);
            var jobConfig = new JobProperties("default-webui-job", envProps);

            var factory = new ExecutionEnvironmentFactory();
            StreamExecutionEnvironment env = factory.create(jobConfig);

            assertAll(
                () -> assertNotNull(env),
                () -> assertTrue(env instanceof LocalStreamEnvironment),
                () -> assertEquals(8082, env.getConfiguration().get(RestOptions.PORT)),
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
        @DisplayName("Should throw UnsupportedExecutionEnvironmentException when localWebUi is enabled on a cluster environment")
        void shouldThrowExceptionWhenLocalWebUiEnabledOnClusterEnvironment() {
            var localWebUiConfig = new LocalWebUiProperties(true, 8081, "127.0.0.1");
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, localWebUiConfig, null);
            var jobConfig = new JobProperties("local-webui-cluster-job", envProps);

            var mockClusterEnv = mock(StreamExecutionEnvironment.class);
            try (var mockedStatic = mockStatic(StreamExecutionEnvironment.class)) {
                mockedStatic.when(StreamExecutionEnvironment::getExecutionEnvironment).thenReturn(mockClusterEnv);

                var factory = new ExecutionEnvironmentFactory();
                assertThrows(UnsupportedExecutionEnvironmentException.class, () -> factory.create(jobConfig));
            }
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
