package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.RestoreMode;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.SavepointRestoreConfiguration;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateRecoveryOptions;

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
        savepointConfig.restoreMode().ifPresent(this::applyRestoreMode);
    }

    private void applySavepointPath(String savepointPath) {
        toConfigure.set(StateRecoveryOptions.SAVEPOINT_PATH, savepointPath);
    }

    private void applyAllowNonRestoredState(boolean allowNonRestoredState) {
        toConfigure.set(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE, allowNonRestoredState);
    }

    private void applyRestoreMode(RestoreMode restoreMode) {
        var flinkRestoreMode = org.apache.flink.core.execution.RestoreMode.valueOf(restoreMode.toString());
        toConfigure.set(StateRecoveryOptions.RESTORE_MODE, flinkRestoreMode);
    }
}
