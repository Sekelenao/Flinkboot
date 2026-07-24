package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendType;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateLatencyTrackOptions;

import java.util.Objects;

public final class StateBackendCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public StateBackendCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.stateBackend().ifPresent(this::apply);
    }

    private void apply(StateBackendConfiguration stateConfig) {
        stateConfig.type().ifPresent(type -> this.applyType(type, stateConfig));
        stateConfig.checkpointStorage().ifPresent(this::applyCheckpointStorage);
        stateConfig.storagePath().ifPresent(this::applyStoragePath);
        stateConfig.incremental().ifPresent(this::applyIncremental);
        stateConfig.latencyTracking().ifPresent(this::applyLatencyTracking);
    }

    private void applyType(StateBackendType type, StateBackendConfiguration stateConfig) {
        switch (type) {
            case HASHMAP:
                toConfigure.set(StateBackendOptions.STATE_BACKEND, "hashmap");
                break;
            case ROCKSDB:
                toConfigure.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
                break;
            case CHANGELOG:
                toConfigure.set(StateBackendOptions.STATE_BACKEND, "changelog");
                break;
            case CUSTOM:
                stateConfig.customClass().ifPresent(this::applyCustomClass);
                break;
        }
    }

    private void applyCustomClass(String customClass) {
        toConfigure.set(StateBackendOptions.STATE_BACKEND, customClass);
    }

    private void applyCheckpointStorage(CheckpointStorageType checkpointStorage) {
        toConfigure.set(CheckpointingOptions.CHECKPOINT_STORAGE, checkpointStorage.toString().toLowerCase());
    }

    private void applyStoragePath(String storagePath) {
        toConfigure.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, storagePath);
    }

    private void applyIncremental(boolean incremental) {
        toConfigure.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, incremental);
    }

    private void applyLatencyTracking(boolean latencyTracking) {
        toConfigure.set(StateLatencyTrackOptions.LATENCY_TRACK_ENABLED, latencyTracking);
    }
}
