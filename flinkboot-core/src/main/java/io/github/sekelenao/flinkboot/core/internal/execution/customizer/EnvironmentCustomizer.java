package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;

public interface EnvironmentCustomizer {

    void configure(ExecutionEnvironmentConfiguration configuration);

}
