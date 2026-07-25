package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.local.LocalWebUiConfiguration;
import io.github.sekelenao.flinkboot.core.internal.annotation.VisibleForTesting;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.CheckpointingCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.EnvironmentCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.ExecutionCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.LocalWebUiCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.PropertiesCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.RestartStrategyCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.SavepointRestoreCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.StateBackendCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ClusterExecutionEnvironmentProvider;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.LocalWebUiExecutionEnvironmentProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Objects;

public final class ExecutionEnvironmentFactory {

    private final Configuration configuration;
    private final ExecutionEnvironmentProvider provider;
    private final List<EnvironmentCustomizer> customizers;

    public ExecutionEnvironmentFactory() {
        this(new ClusterExecutionEnvironmentProvider());
    }

    @VisibleForTesting
    ExecutionEnvironmentFactory(ExecutionEnvironmentProvider provider) {
        this.configuration = new Configuration();
        this.provider = Objects.requireNonNull(provider);
        this.customizers = List.of(
            new ExecutionCustomizer(configuration),
            new CheckpointingCustomizer(configuration),
            new RestartStrategyCustomizer(configuration),
            new StateBackendCustomizer(configuration),
            new SavepointRestoreCustomizer(configuration),
            new LocalWebUiCustomizer(configuration),
            new PropertiesCustomizer(configuration)
        );
    }

    public StreamExecutionEnvironment create(JobConfiguration jobConfiguration) {
        Objects.requireNonNull(jobConfiguration);
        configuration.set(PipelineOptions.NAME, jobConfiguration.name());
        jobConfiguration.environment().ifPresent(envConfig ->
            customizers.forEach(customizer -> customizer.configure(envConfig))
        );

        ExecutionEnvironmentProvider resolvedProvider = resolveProvider(jobConfiguration);
        return resolvedProvider.createEnvironment(configuration);
    }

    private ExecutionEnvironmentProvider resolveProvider(JobConfiguration jobConfiguration) {
        boolean useLocalWebUi = jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::localWebUi)
            .flatMap(LocalWebUiConfiguration::enabled)
            .orElse(false);

        if (useLocalWebUi) {
            return new LocalWebUiExecutionEnvironmentProvider();
        }
        return provider;
    }
}

