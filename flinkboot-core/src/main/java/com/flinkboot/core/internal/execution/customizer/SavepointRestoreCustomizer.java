package com.flinkboot.core.internal.execution.customizer;

import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.savepoint.RestoreMode;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateRecoveryOptions;

import java.util.Objects;

public final class SavepointRestoreCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public SavepointRestoreCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentProperties configuration) {
        Objects.requireNonNull(configuration);
        configuration.savepointRestore().ifPresent(this::apply);
    }

    private void apply(SavepointRestoreProperties savepointConfig) {
        savepointConfig.directory().ifPresent(dir -> toConfigure.set(CheckpointingOptions.SAVEPOINT_DIRECTORY, dir));
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
