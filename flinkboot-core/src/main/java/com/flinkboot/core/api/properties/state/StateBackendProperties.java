package com.flinkboot.core.api.properties.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flinkboot.core.api.validation.ValidatableProperties;
import com.flinkboot.core.internal.annotation.Generated;
import com.flinkboot.core.internal.validation.properties.StateBackendPropertiesValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration properties for Flink state backends and checkpoint storage.
 * <p>
 * Supports {@link StateBackendType#HASHMAP}, {@link StateBackendType#ROCKSDB},
 * {@link StateBackendType#CUSTOM}.
 */
public final class StateBackendProperties implements Serializable, ValidatableProperties {

    private static final long serialVersionUID = 1L;

    private final StateBackendType type;
    private final CheckpointStorageType checkpointStorage;
    private final Boolean incremental;
    private final Boolean latencyTracking;
    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String customClass;
    private final Boolean changelog;

    /**
     * Creates a new {@code StateBackendProperties} instance.
     *
     * @param type              the state backend type (HASHMAP, ROCKSDB, CHANGELOG, CUSTOM)
     * @param checkpointStorage the checkpoint storage type (JOBMANAGER, FILESYSTEM)
     * @param incremental       whether incremental checkpoints are enabled (RocksDB)
     * @param latencyTracking   whether state access latency tracking metrics are enabled (RocksDB)
     * @param customClass       fully qualified class name of custom state backend factory
     * @param changelog        whether the changelog state backend feature is enabled
     */
    @JsonCreator
    public StateBackendProperties(
        @JsonProperty("type") StateBackendType type,
        @JsonProperty("checkpoint-storage") CheckpointStorageType checkpointStorage,
        @JsonProperty("incremental") Boolean incremental,
        @JsonProperty("latency-tracking") Boolean latencyTracking,
        @JsonProperty("custom-class") String customClass,
        @JsonProperty("changelog") Boolean changelog
    ) {
        this.type = type;
        this.checkpointStorage = checkpointStorage;
        this.incremental = incremental;
        this.latencyTracking = latencyTracking;
        this.customClass = customClass;
        this.changelog = changelog;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return StateBackendPropertiesValidator.validate(this, context);
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

    /**
     * Returns whether the changelog state backend feature is enabled.
     *
     * @return an Optional containing the changelog flag, or empty if not specified
     */
    public Optional<Boolean> changelog() {
        return Optional.ofNullable(changelog);
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
            && Objects.equals(incremental, o.incremental)
            && Objects.equals(latencyTracking, o.latencyTracking)
            && Objects.equals(customClass, o.customClass)
            && Objects.equals(changelog, o.changelog);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(
            type,
            checkpointStorage,
            incremental,
            latencyTracking,
            customClass,
            changelog
        );
    }

    @Override
    @Generated
    public String toString() {
        return "StateBackendProperties{" +
            "type=" + type +
            ", checkpointStorage=" + checkpointStorage +
            ", incremental=" + incremental +
            ", latencyTracking=" + latencyTracking +
            ", customClass='" + customClass + '\'' +
            ", changelog=" + changelog +
            '}';
    }
}
