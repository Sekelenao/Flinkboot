package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionRuntimeMode;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ClusterExecutionEnvironmentProvider;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
        @DisplayName("Should throw NullPointerException when jobConfiguration is null")
        void shouldThrowNpeWhenJobConfigurationIsNull() {
            var factory = new ExecutionEnvironmentFactory(new ClusterExecutionEnvironmentProvider());
            assertThrows(NullPointerException.class, () -> factory.createFlinkConfiguration(null));
        }

        @Test
        @DisplayName("Should correctly map JobConfiguration into Flink Configuration")
        void shouldMapJobConfigurationToFlinkConfiguration() {
            var execConfig = new ExecutionConfiguration(
                ExecutionRuntimeMode.STREAMING,
                8,
                128,
                100L,
                200L,
                true
            );
            var envConfig = new ExecutionEnvironmentConfiguration(execConfig);
            var jobConfig = new JobConfiguration("my-test-job", envConfig);

            var factory = new ExecutionEnvironmentFactory(new ClusterExecutionEnvironmentProvider());
            Configuration flinkConfig = factory.createFlinkConfiguration(jobConfig);

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
    }

    @Nested
    @DisplayName("Provider Delegation Tests")
    class ProviderDelegationTests {

        @Test
        @DisplayName("Should throw NullPointerException when provider is null in constructor")
        void shouldThrowNpeWhenProviderIsNull() {
            assertThrows(NullPointerException.class, () -> new ExecutionEnvironmentFactory(null));
        }

        @Test
        @DisplayName("Should delegate environment creation to the configured provider")
        void shouldDelegateToProvider() {
            var dummyEnv = StreamExecutionEnvironment.getExecutionEnvironment();
            ExecutionEnvironmentProvider customProvider = config -> dummyEnv;

            var factory = new ExecutionEnvironmentFactory(customProvider);
            var jobConfig = new JobConfiguration("my-test-job", null);

            StreamExecutionEnvironment env = factory.createExecutionEnvironment(jobConfig);

            assertNotNull(env);
            assertEquals(dummyEnv, env);
        }
    }
}
