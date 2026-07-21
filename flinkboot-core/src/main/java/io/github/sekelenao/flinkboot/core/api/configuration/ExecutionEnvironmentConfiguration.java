package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.RestartStrategyConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendConfiguration;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;

import java.io.Serializable;
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

    @JsonCreator
    public ExecutionEnvironmentConfiguration(
        @JsonProperty("execution") ExecutionConfiguration execution,
        @JsonProperty("checkpointing") CheckpointingConfiguration checkpointing,
        @JsonProperty("restart-strategy") RestartStrategyConfiguration restartStrategy,
        @JsonProperty("state-backend") StateBackendConfiguration stateBackend
    ) {
        this.execution = execution;
        this.checkpointing = checkpointing;
        this.restartStrategy = restartStrategy;
        this.stateBackend = stateBackend;
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
            Objects.equals(stateBackend, o.stateBackend);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(execution, checkpointing, restartStrategy, stateBackend);
    }

    @Override
    @Generated
    public String toString() {
        return "ExecutionEnvironmentConfiguration{" +
            "execution=" + execution +
            ", checkpointing=" + checkpointing +
            ", restartStrategy=" + restartStrategy +
            ", stateBackend=" + stateBackend +
            '}';
    }
}
