package io.github.sekelenao.flinkboot.core.api.configuration.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendConfigurationException;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class StateBackendConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StateBackendType type;
    private final CheckpointStorageType checkpointStorage;
    private final String storagePath;
    private final Boolean incremental;
    private final Boolean latencyTracking;
    private final String customClass;

    @JsonCreator
    public StateBackendConfiguration(
        @JsonProperty("type") StateBackendType type,
        @JsonProperty("checkpoint-storage") CheckpointStorageType checkpointStorage,
        @JsonProperty("storage-path") String storagePath,
        @JsonProperty("incremental") Boolean incremental,
        @JsonProperty("latency-tracking") Boolean latencyTracking,
        @JsonProperty("custom-class") String customClass
    ) {
        this.type = type;
        this.checkpointStorage = checkpointStorage;
        this.storagePath = storagePath;
        this.incremental = incremental;
        this.latencyTracking = latencyTracking;
        this.customClass = customClass;
        validate();
    }

    public Optional<StateBackendType> type() {
        return Optional.ofNullable(type);
    }

    public Optional<CheckpointStorageType> checkpointStorage() {
        return Optional.ofNullable(checkpointStorage);
    }

    public Optional<String> storagePath() {
        return Optional.ofNullable(storagePath);
    }

    public Optional<Boolean> incremental() {
        return Optional.ofNullable(incremental);
    }

    public Optional<Boolean> latencyTracking() {
        return Optional.ofNullable(latencyTracking);
    }

    public Optional<String> customClass() {
        return Optional.ofNullable(customClass);
    }

    private void validate() {
        if (type == StateBackendType.CUSTOM) {
            if (customClass == null || customClass.isBlank()) {
                throw new InvalidStateBackendConfigurationException(
                    "custom-class must be specified when state backend type is CUSTOM"
                );
            }
        } else if (customClass != null && !customClass.isBlank()) {
            throw new InvalidStateBackendConfigurationException(
                "custom-class can only be specified when state backend type is CUSTOM"
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateBackendConfiguration that = (StateBackendConfiguration) o;
        return type == that.type &&
            checkpointStorage == that.checkpointStorage &&
            Objects.equals(storagePath, that.storagePath) &&
            Objects.equals(incremental, that.incremental) &&
            Objects.equals(latencyTracking, that.latencyTracking) &&
            Objects.equals(customClass, that.customClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            type,
            checkpointStorage,
            storagePath,
            incremental,
            latencyTracking,
            customClass
        );
    }

    @Override
    public String toString() {
        return "StateBackendConfiguration{" +
            "type=" + type +
            ", checkpointStorage=" + checkpointStorage +
            ", storagePath='" + storagePath + '\'' +
            ", incremental=" + incremental +
            ", latencyTracking=" + latencyTracking +
            ", customClass='" + customClass + '\'' +
            '}';
    }
}
