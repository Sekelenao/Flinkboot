package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import org.apache.flink.configuration.Configuration;

import java.util.Objects;

public final class PropertiesCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public PropertiesCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.properties().forEach(this::applyProperty);
    }

    private void applyProperty(String key, String value) {
        toConfigure.setString(key, value);
    }
}
