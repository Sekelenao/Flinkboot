package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.properties.local.LocalWebUiProperties;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.CheckpointingCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.EnvironmentCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.ExecutionCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.LocalWebUiCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.PropertiesCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.RestartStrategyCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.SavepointRestoreCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.customizer.StateBackendCustomizer;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Objects;

public final class ExecutionEnvironmentFactory {

    private final Configuration configuration;
    private final List<EnvironmentCustomizer> customizers;

    public ExecutionEnvironmentFactory() {
        this.configuration = new Configuration();
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

    public StreamExecutionEnvironment create(JobProperties jobProperties) {
        Objects.requireNonNull(jobProperties);
        configuration.set(PipelineOptions.NAME, jobProperties.name());
        jobProperties.environment().ifPresent(envProps ->
            customizers.forEach(customizer -> customizer.configure(envProps))
        );

        boolean useLocalWebUi = jobProperties.environment()
            .flatMap(ExecutionEnvironmentProperties::localWebUi)
            .flatMap(LocalWebUiProperties::enabled)
            .orElse(false);

        return ExecutionEnvironmentProvider.get(useLocalWebUi).createEnvironment(configuration);
    }
}
