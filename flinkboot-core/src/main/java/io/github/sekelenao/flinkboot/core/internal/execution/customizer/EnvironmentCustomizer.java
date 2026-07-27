package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;

public interface EnvironmentCustomizer {

    void configure(ExecutionEnvironmentProperties configuration);

}
