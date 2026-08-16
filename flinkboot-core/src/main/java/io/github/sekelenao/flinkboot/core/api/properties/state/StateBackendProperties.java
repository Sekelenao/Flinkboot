package io.github.sekelenao.flinkboot.core.api.properties.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendPropertiesException;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration properties for Flink state backends and checkpoint storage.
 * <p>
 * Supports {@link StateBackendType#HASHMAP}, {@link StateBackendType#ROCKSDB},
 * {@link StateBackendType#CHANGELOG}, or {@link StateBackendType#CUSTOM}.
 */
public final class StateBackendProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StateBackendType type;
    private final CheckpointStorageType checkpointStorage;
    private final String storagePath;
    private final Boolean incremental;
    private final Boolean latencyTracking;
    private final String customClass;

    /**
     * Creates a new {@code StateBackendProperties} instance.
     *
     * @param type              the state backend type (HASHMAP, ROCKSDB, CHANGELOG, CUSTOM)
     * @param checkpointStorage the checkpoint storage type (JOBMANAGER, FILESYSTEM)
     * @param storagePath       the root storage path for checkpoints
     * @param incremental       whether incremental checkpoints are enabled (RocksDB)
     * @param latencyTracking   whether state access latency tracking metrics are enabled (RocksDB)
     * @param customClass       fully qualified class name of custom state backend factory
     * @throws InvalidStateBackendPropertiesException if customClass is invalid for the specified type
     */
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

    /**
     * Returns the optional state backend type.
     *
     * @return an {@link Optional} containing the {@link StateBackendType}, or empty if not specified
     */
    public Optional<StateBackendType> type() {
        return Optional.ofNullable(type);
    }

    /**
     * Returns the optional checkpoint storage type.
     *
     * @return an {@link Optional} containing the {@link CheckpointStorageType}, or empty if not specified
     */
    public Optional<CheckpointStorageType> checkpointStorage() {
        return Optional.ofNullable(checkpointStorage);
    }

    /**
     * Returns the optional checkpoint storage directory path.
     *
     * @return an {@link Optional} containing the storage path string, or empty if not specified
     */
    public Optional<String> storagePath() {
        return Optional.ofNullable(storagePath);
    }

    /**
     * Returns whether incremental checkpointing is enabled.
     *
     * @return an {@link Optional} containing the incremental flag, or empty if not specified
     */
    public Optional<Boolean> incremental() {
        return Optional.ofNullable(incremental);
    }

    /**
     * Returns whether latency tracking metrics are enabled.
     *
     * @return an {@link Optional} containing the latency tracking flag, or empty if not specified
     */
    public Optional<Boolean> latencyTracking() {
        return Optional.ofNullable(latencyTracking);
    }

    /**
     * Returns the custom state backend factory class name.
     *
     * @return an {@link Optional} containing the custom class name, or empty if not specified
     */
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
