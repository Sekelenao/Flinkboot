package io.github.sekelenao.flinkboot.core.api.properties.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendPropertiesException;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class StateBackendProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StateBackendType type;
    private final CheckpointStorageType checkpointStorage;
    private final String storagePath;
    private final Boolean incremental;
    private final Boolean latencyTracking;
    private final String customClass;

    @JsonCreator
    public StateBackendProperties(
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
                throw new InvalidStateBackendPropertiesException(
                    "custom-class must be specified when state backend type is CUSTOM"
                );
            }
        } else if (customClass != null && !customClass.isBlank()) {
            throw new InvalidStateBackendPropertiesException(
                "custom-class can only be specified when state backend type is CUSTOM"
            );
        }
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof StateBackendProperties)) {
            return false;
        }
        var o = (StateBackendProperties) other;
        return type == o.type
            && checkpointStorage == o.checkpointStorage
            && Objects.equals(storagePath, o.storagePath)
            && Objects.equals(incremental, o.incremental)
            && Objects.equals(latencyTracking, o.latencyTracking)
            && Objects.equals(customClass, o.customClass);
    }

    @Override
    @Generated
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
    @Generated
    public String toString() {
        return "StateBackendProperties{" +
            "type=" + type +
            ", checkpointStorage=" + checkpointStorage +
            ", storagePath='" + storagePath + '\'' +
            ", incremental=" + incremental +
            ", latencyTracking=" + latencyTracking +
            ", customClass='" + customClass + '\'' +
            '}';
    }
}
