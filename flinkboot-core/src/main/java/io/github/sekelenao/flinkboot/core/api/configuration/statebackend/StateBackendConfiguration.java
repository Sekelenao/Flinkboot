package io.github.sekelenao.flinkboot.core.api.configuration.statebackend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class StateBackendConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private final FlinkStateBackendType type;

    private final String checkpointStorage;

    private final Boolean incremental;

    @JsonCreator
    public StateBackendConfiguration(
        @JsonProperty("type") FlinkStateBackendType type,
        @JsonProperty("checkpoint-storage") String checkpointStorage,
        @JsonProperty("incremental") Boolean incremental
    ) {
        this.type = Objects.requireNonNull(type);
        this.checkpointStorage = checkpointStorage;
        this.incremental = incremental;
    }

    public FlinkStateBackendType type() {
        return type;
    }

    public Optional<String> checkpointStorage() {
        return Optional.ofNullable(checkpointStorage);
    }

    public Optional<Boolean> incremental() {
        return Optional.ofNullable(incremental);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof StateBackendConfiguration)) {
            return false;
        }
        var otherConfig = (StateBackendConfiguration) other;
        return Objects.equals(type, otherConfig.type)
            && Objects.equals(checkpointStorage, otherConfig.checkpointStorage)
            && Objects.equals(incremental, otherConfig.incremental);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(type, checkpointStorage, incremental);
    }

    @Override
    @Generated
    public String toString() {
        return "StateBackendConfiguration{" +
            "type='" + type + '\'' +
            ", checkpointStorage='" + checkpointStorage + '\'' +
            ", incremental=" + incremental +
            '}';
    }
}
