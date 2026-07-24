package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.SavepointRestoreConfiguration;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.apache.flink.core.execution.RestoreMode;

import java.util.Objects;

public final class SavepointRestoreCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public SavepointRestoreCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.savepointRestore().ifPresent(this::apply);
    }

    private void apply(SavepointRestoreConfiguration savepointConfig) {
        this.applySavepointPath(savepointConfig.savepointPath());
        savepointConfig.allowNonRestoredState().ifPresent(this::applyAllowNonRestoredState);
        this.applyRestoreMode(savepointConfig);
    }

    private void applySavepointPath(String savepointPath) {
        toConfigure.set(StateRecoveryOptions.SAVEPOINT_PATH, savepointPath);
    }

    private void applyAllowNonRestoredState(boolean allowNonRestoredState) {
        toConfigure.set(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE, allowNonRestoredState);
    }

    private void applyRestoreMode(SavepointRestoreConfiguration savepointConfig) {
        savepointConfig.restoreMode().ifPresent(mode ->
            toConfigure.set(StateRecoveryOptions.RESTORE_MODE, RestoreMode.valueOf(mode.toString()))
        );
    }
}
