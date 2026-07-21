package io.github.sekelenao.flinkboot.core.api.configuration.checkpointing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class CheckpointingConfiguration {

    private final Boolean enabled;

    @Positive
    private final Long intervalMs;

    private final CheckpointingMode mode;

    @Positive
    private final Long timeoutMs;

    @PositiveOrZero
    private final Long minPauseBetweenCheckpointsMs;

    @Positive
    private final Integer maxConcurrentCheckpoints;

    private final ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup;

    private final Boolean unalignedCheckpoints;

    @PositiveOrZero
    private final Long alignedCheckpointTimeoutMs;

    private final String storageUri;

    @JsonCreator
    public CheckpointingConfiguration(
        @JsonProperty("enabled") Boolean enabled,
        @JsonProperty("interval-ms") Long intervalMs,
        @JsonProperty("mode") CheckpointingMode mode,
        @JsonProperty("timeout-ms") Long timeoutMs,
        @JsonProperty("min-pause-between-checkpoints-ms") Long minPauseBetweenCheckpointsMs,
        @JsonProperty("max-concurrent-checkpoints") Integer maxConcurrentCheckpoints,
        @JsonProperty("externalized-checkpoint-cleanup") ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup,
        @JsonProperty("unaligned-checkpoints") Boolean unalignedCheckpoints,
        @JsonProperty("aligned-checkpoint-timeout-ms") Long alignedCheckpointTimeoutMs,
        @JsonProperty("storage-uri") String storageUri
    ) {
        this.enabled = enabled;
        this.intervalMs = intervalMs;
        this.mode = mode;
        this.timeoutMs = timeoutMs;
        this.minPauseBetweenCheckpointsMs = minPauseBetweenCheckpointsMs;
        this.maxConcurrentCheckpoints = maxConcurrentCheckpoints;
        this.externalizedCheckpointCleanup = externalizedCheckpointCleanup;
        this.unalignedCheckpoints = unalignedCheckpoints;
        this.alignedCheckpointTimeoutMs = alignedCheckpointTimeoutMs;
        this.storageUri = storageUri;
    }

    public Optional<Boolean> enabled() {
        return Optional.ofNullable(enabled);
    }

    public OptionalLong intervalMs() {
        if (intervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(intervalMs);
    }

    public Optional<CheckpointingMode> mode() {
        return Optional.ofNullable(mode);
    }

    public OptionalLong timeoutMs() {
        if (timeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(timeoutMs);
    }

    public OptionalLong minPauseBetweenCheckpointsMs() {
        if (minPauseBetweenCheckpointsMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(minPauseBetweenCheckpointsMs);
    }

    public OptionalInt maxConcurrentCheckpoints() {
        if (maxConcurrentCheckpoints == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxConcurrentCheckpoints);
    }

    public Optional<ExternalizedCheckpointCleanupMode> externalizedCheckpointCleanup() {
        return Optional.ofNullable(externalizedCheckpointCleanup);
    }

    public Optional<Boolean> unalignedCheckpoints() {
        return Optional.ofNullable(unalignedCheckpoints);
    }

    public OptionalLong alignedCheckpointTimeoutMs() {
        if (alignedCheckpointTimeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(alignedCheckpointTimeoutMs);
    }

    public Optional<String> storageUri() {
        return Optional.ofNullable(storageUri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckpointingConfiguration that = (CheckpointingConfiguration) o;
        return Objects.equals(enabled, that.enabled) &&
            Objects.equals(intervalMs, that.intervalMs) &&
            mode == that.mode &&
            Objects.equals(timeoutMs, that.timeoutMs) &&
            Objects.equals(minPauseBetweenCheckpointsMs, that.minPauseBetweenCheckpointsMs) &&
            Objects.equals(maxConcurrentCheckpoints, that.maxConcurrentCheckpoints) &&
            externalizedCheckpointCleanup == that.externalizedCheckpointCleanup &&
            Objects.equals(unalignedCheckpoints, that.unalignedCheckpoints) &&
            Objects.equals(alignedCheckpointTimeoutMs, that.alignedCheckpointTimeoutMs) &&
            Objects.equals(storageUri, that.storageUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            enabled,
            intervalMs,
            mode,
            timeoutMs,
            minPauseBetweenCheckpointsMs,
            maxConcurrentCheckpoints,
            externalizedCheckpointCleanup,
            unalignedCheckpoints,
            alignedCheckpointTimeoutMs,
            storageUri
        );
    }
}
