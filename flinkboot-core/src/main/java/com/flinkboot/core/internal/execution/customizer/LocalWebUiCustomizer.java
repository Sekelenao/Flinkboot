package com.flinkboot.core.internal.execution.customizer;

import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.local.LocalWebUiProperties;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;

import java.util.Objects;

public final class LocalWebUiCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public LocalWebUiCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentProperties configuration) {
        Objects.requireNonNull(configuration);
        configuration.localWebUi()
            .filter(localWebUi -> localWebUi.enabled().orElse(true))
            .ifPresent(this::apply);
    }

    private void apply(LocalWebUiProperties localWebUiConfig) {
        localWebUiConfig.port().ifPresent(this::applyPort);
        localWebUiConfig.bindAddress().ifPresent(this::applyBindAddress);
    }

    private void applyPort(int port) {
        toConfigure.set(RestOptions.PORT, port);
    }

    private void applyBindAddress(String bindAddress) {
        toConfigure.set(RestOptions.BIND_ADDRESS, bindAddress);
    }
}
