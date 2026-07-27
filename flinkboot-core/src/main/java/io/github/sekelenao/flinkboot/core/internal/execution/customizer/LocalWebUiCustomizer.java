package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.local.LocalWebUiProperties;
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
        configuration.localWebUi().ifPresent(this::apply);
    }

    private void apply(LocalWebUiProperties localWebUiConfig) {
        if (!localWebUiConfig.enabled().orElse(false)) {
            return;
        }
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
