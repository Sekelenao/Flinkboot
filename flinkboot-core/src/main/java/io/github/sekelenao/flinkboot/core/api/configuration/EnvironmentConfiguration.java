package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public final class EnvironmentConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Pattern(regexp = "STREAMING|BATCH|AUTOMATIC")
    private final String runtimeMode;

    private final Boolean localWebUi;

    @Min(1)
    @Max(65535)
    private final Integer localWebUiPort;

    @Min(1)
    private final Integer parallelism;

    @Min(1)
    private final Integer maxParallelism;

    @Valid
    private final CheckpointingConfiguration checkpointing;

    @Valid
    private final StateBackendConfiguration stateBackend;

    @Valid
    private final RestartStrategyConfiguration restartStrategy;

    @JsonCreator
    public EnvironmentConfiguration(
        @JsonProperty("runtimeMode") String runtimeMode,
        @JsonProperty("localWebUi") Boolean localWebUi,
        @JsonProperty("localWebUiPort") Integer localWebUiPort,
        @JsonProperty("parallelism") Integer parallelism,
        @JsonProperty("maxParallelism") Integer maxParallelism,
        @JsonProperty("checkpointing") CheckpointingConfiguration checkpointing,
        @JsonProperty("stateBackend") StateBackendConfiguration stateBackend,
        @JsonProperty("restartStrategy") RestartStrategyConfiguration restartStrategy
    ) {
        this.runtimeMode = runtimeMode;
        this.localWebUi = localWebUi;
        this.localWebUiPort = localWebUiPort;
        this.parallelism = parallelism;
        this.maxParallelism = maxParallelism;
        this.checkpointing = checkpointing;
        this.stateBackend = stateBackend;
        this.restartStrategy = restartStrategy;
    }

    public Optional<String> runtimeMode() {
        return Optional.ofNullable(runtimeMode);
    }

    public Optional<Boolean> localWebUi() {
        return Optional.ofNullable(localWebUi);
    }

    public OptionalInt localWebUiPort() {
        return localWebUiPort == null ? OptionalInt.empty() : OptionalInt.of(localWebUiPort);
    }

    public OptionalInt parallelism() {
        return parallelism == null ? OptionalInt.empty() : OptionalInt.of(parallelism);
    }

    public OptionalInt maxParallelism() {
        return maxParallelism == null ? OptionalInt.empty() : OptionalInt.of(maxParallelism);
    }

    public Optional<CheckpointingConfiguration> checkpointing() {
        return Optional.ofNullable(checkpointing);
    }

    public Optional<StateBackendConfiguration> stateBackend() {
        return Optional.ofNullable(stateBackend);
    }

    public Optional<RestartStrategyConfiguration> restartStrategy() {
        return Optional.ofNullable(restartStrategy);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof EnvironmentConfiguration)) {
            return false;
        }
        var otherConfig = (EnvironmentConfiguration) other;
        return Objects.equals(runtimeMode, otherConfig.runtimeMode)
            && Objects.equals(localWebUi, otherConfig.localWebUi)
            && Objects.equals(localWebUiPort, otherConfig.localWebUiPort)
            && Objects.equals(parallelism, otherConfig.parallelism)
            && Objects.equals(maxParallelism, otherConfig.maxParallelism)
            && Objects.equals(checkpointing, otherConfig.checkpointing)
            && Objects.equals(stateBackend, otherConfig.stateBackend)
            && Objects.equals(restartStrategy, otherConfig.restartStrategy);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(
            runtimeMode,
            localWebUi,
            localWebUiPort,
            parallelism,
            maxParallelism,
            checkpointing,
            stateBackend,
            restartStrategy
        );
    }

    @Override
    @Generated
    public String toString() {
        return "EnvironmentConfiguration{" +
            "runtimeMode='" + runtimeMode + '\'' +
            ", localWebUi=" + localWebUi +
            ", localWebUiPort=" + localWebUiPort +
            ", parallelism=" + parallelism +
            ", maxParallelism=" + maxParallelism +
            ", checkpointing=" + checkpointing +
            ", stateBackend=" + stateBackend +
            ", restartStrategy=" + restartStrategy +
            '}';
    }
}
