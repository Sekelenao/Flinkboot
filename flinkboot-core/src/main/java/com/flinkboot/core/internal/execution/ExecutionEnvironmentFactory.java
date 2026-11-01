package com.flinkboot.core.internal.execution;

import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.JobProperties;
import com.flinkboot.core.internal.execution.customizer.CheckpointingCustomizer;
import com.flinkboot.core.internal.execution.customizer.EnvironmentCustomizer;
import com.flinkboot.core.internal.execution.customizer.ExecutionCustomizer;
import com.flinkboot.core.internal.execution.customizer.LocalWebUiCustomizer;
import com.flinkboot.core.internal.execution.customizer.PropertiesCustomizer;
import com.flinkboot.core.internal.execution.customizer.RestartStrategyCustomizer;
import com.flinkboot.core.internal.execution.customizer.SavepointRestoreCustomizer;
import com.flinkboot.core.internal.execution.customizer.StateBackendCustomizer;
import com.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
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
        Objects.requireNonNull(jobProperties, "jobProperties must not be null");
        configuration.set(PipelineOptions.NAME, jobProperties.name());
        jobProperties.environment().ifPresent(envProps ->
            customizers.forEach(customizer -> customizer.configure(envProps))
        );

        boolean useLocalWebUi = jobProperties.environment()
            .flatMap(ExecutionEnvironmentProperties::localWebUi)
            .filter(ui -> ui.enabled().orElse(true))
            .isPresent();

        return ExecutionEnvironmentProvider.get(useLocalWebUi).createEnvironment(configuration);
    }
}
