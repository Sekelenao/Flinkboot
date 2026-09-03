package io.github.sekelenao.flinkboot.core.api.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.local.LocalWebUiProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration properties for setting up a Flink execution environment.
 * <p>
 * Encapsulates execution settings, checkpointing, restart strategies, state backends,
 * savepoint restoration, local Web UI, and arbitrary key-value properties.
 */
public final class ExecutionEnvironmentProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private final ExecutionProperties execution;

    @Valid
    private final CheckpointingProperties checkpointing;

    @Valid
    private final RestartStrategyProperties restartStrategy;

    @Valid
    private final StateBackendProperties stateBackend;

    @Valid
    private final SavepointRestoreProperties savepointRestore;

    @Valid
    private final LocalWebUiProperties localWebUi;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code ExecutionEnvironmentProperties} instance.
     *
     * @param execution        runtime execution properties
     * @param checkpointing    checkpointing settings
     * @param restartStrategy  failure restart strategy
     * @param stateBackend     state backend and checkpoint storage configuration
     * @param savepointRestore savepoint restore settings
     * @param localWebUi       local development Web UI configuration
     * @param properties       additional raw Flink configuration key-value properties
     */
    @JsonCreator
    public ExecutionEnvironmentProperties(
        @JsonProperty("execution") ExecutionProperties execution,
        @JsonProperty("checkpointing") CheckpointingProperties checkpointing,
        @JsonProperty("restart-strategy") RestartStrategyProperties restartStrategy,
        @JsonProperty("state-backend") StateBackendProperties stateBackend,
        @JsonProperty("savepoint-restore") SavepointRestoreProperties savepointRestore,
        @JsonProperty("local-web-ui") LocalWebUiProperties localWebUi,
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

    /**
     * Returns the optional execution properties.
     *
     * @return an {@link Optional} containing execution properties, or empty if not configured
     */
    public Optional<ExecutionProperties> execution() {
        return Optional.ofNullable(execution);
    }

    /**
     * Returns the optional checkpointing properties.
     *
     * @return an {@link Optional} containing checkpointing properties, or empty if not configured
     */
    public Optional<CheckpointingProperties> checkpointing() {
        return Optional.ofNullable(checkpointing);
    }

    /**
     * Returns the optional restart strategy configuration.
     *
     * @return an {@link Optional} containing the restart strategy properties, or empty if not configured
     */
    public Optional<RestartStrategyProperties> restartStrategy() {
        return Optional.ofNullable(restartStrategy);
    }

    /**
     * Returns the optional state backend configuration.
     *
     * @return an {@link Optional} containing state backend properties, or empty if not configured
     */
    public Optional<StateBackendProperties> stateBackend() {
        return Optional.ofNullable(stateBackend);
    }

    /**
     * Returns the optional savepoint restore configuration.
     *
     * @return an {@link Optional} containing savepoint restore properties, or empty if not configured
     */
    public Optional<SavepointRestoreProperties> savepointRestore() {
        return Optional.ofNullable(savepointRestore);
    }

    /**
     * Returns the optional local Web UI configuration.
     *
     * @return an {@link Optional} containing local Web UI properties, or empty if not configured
     */
    public Optional<LocalWebUiProperties> localWebUi() {
        return Optional.ofNullable(localWebUi);
    }

    /**
     * Returns the additional key-value configuration properties.
     *
     * @return an unmodifiable map of raw Flink configuration properties
     */
    public Map<String, String> properties() {
        if (properties == null) {
            return Map.of();
        }
        return Map.copyOf(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionEnvironmentProperties)) {
            return false;
        }
        var o = (ExecutionEnvironmentProperties) other;
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
        return "ExecutionEnvironmentProperties{" +
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
