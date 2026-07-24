package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.local.LocalWebUiConfiguration;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;

import java.util.Objects;

public final class LocalWebUiCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public LocalWebUiCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.localWebUi().ifPresent(this::apply);
    }

    private void apply(LocalWebUiConfiguration localWebUiConfig) {
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
