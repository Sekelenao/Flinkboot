package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.local.LocalWebUiConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.RestartStrategyConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.SavepointRestoreConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendConfiguration;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ExecutionEnvironmentConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private final ExecutionConfiguration execution;

    @Valid
    private final CheckpointingConfiguration checkpointing;

    @Valid
    private final RestartStrategyConfiguration restartStrategy;

    @Valid
    private final StateBackendConfiguration stateBackend;

    @Valid
    private final SavepointRestoreConfiguration savepointRestore;

    @Valid
    private final LocalWebUiConfiguration localWebUi;

    private final Map<String, String> properties;

    @JsonCreator
    public ExecutionEnvironmentConfiguration(
        @JsonProperty("execution") ExecutionConfiguration execution,
        @JsonProperty("checkpointing") CheckpointingConfiguration checkpointing,
        @JsonProperty("restart-strategy") RestartStrategyConfiguration restartStrategy,
        @JsonProperty("state-backend") StateBackendConfiguration stateBackend,
        @JsonProperty("savepoint-restore") SavepointRestoreConfiguration savepointRestore,
        @JsonProperty("local-web-ui") LocalWebUiConfiguration localWebUi,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.execution = execution;
        this.checkpointing = checkpointing;
        this.restartStrategy = restartStrategy;
        this.stateBackend = stateBackend;
        this.savepointRestore = savepointRestore;
        this.localWebUi = localWebUi;
        this.properties = properties;
    }

    public Optional<ExecutionConfiguration> execution() {
        return Optional.ofNullable(execution);
    }

    public Optional<CheckpointingConfiguration> checkpointing() {
        return Optional.ofNullable(checkpointing);
    }

    public Optional<RestartStrategyConfiguration> restartStrategy() {
        return Optional.ofNullable(restartStrategy);
    }

    public Optional<StateBackendConfiguration> stateBackend() {
        return Optional.ofNullable(stateBackend);
    }

    public Optional<SavepointRestoreConfiguration> savepointRestore() {
        return Optional.ofNullable(savepointRestore);
    }

    public Optional<LocalWebUiConfiguration> localWebUi() {
        return Optional.ofNullable(localWebUi);
    }

    public Map<String, String> properties() {
        if (properties == null) {
            return Map.of();
        }
        return Map.copyOf(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionEnvironmentConfiguration)) {
            return false;
        }
        var o = (ExecutionEnvironmentConfiguration) other;
        return Objects.equals(execution, o.execution) &&
            Objects.equals(checkpointing, o.checkpointing) &&
            Objects.equals(restartStrategy, o.restartStrategy) &&
            Objects.equals(stateBackend, o.stateBackend) &&
            Objects.equals(savepointRestore, o.savepointRestore) &&
            Objects.equals(localWebUi, o.localWebUi) &&
            Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(execution, checkpointing, restartStrategy, stateBackend, savepointRestore, localWebUi, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "ExecutionEnvironmentConfiguration{" +
            "execution=" + execution +
            ", checkpointing=" + checkpointing +
            ", restartStrategy=" + restartStrategy +
            ", stateBackend=" + stateBackend +
            ", savepointRestore=" + savepointRestore +
            ", localWebUi=" + localWebUi +
            ", properties=" + properties +
            '}';
    }
}
