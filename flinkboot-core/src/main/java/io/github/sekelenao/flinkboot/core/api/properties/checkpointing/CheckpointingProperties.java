package io.github.sekelenao.flinkboot.core.api.properties.checkpointing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Configuration properties for Flink stream checkpointing.
 * <p>
 * Configures interval, timeout, consistency mode, concurrency, unaligned checkpoints,
 * externalized checkpoint cleanup, and storage URI.
 */
public final class CheckpointingProperties implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /**
     * Creates a new {@code CheckpointingProperties} instance.
     *
     * @param enabled                       whether checkpointing is enabled
     * @param intervalMs                    checkpoint interval in milliseconds
     * @param mode                          consistency mode (EXACTLY_ONCE or AT_LEAST_ONCE)
     * @param timeoutMs                     checkpoint timeout in milliseconds
     * @param minPauseBetweenCheckpointsMs  minimum pause duration between consecutive checkpoints in milliseconds
     * @param maxConcurrentCheckpoints      maximum number of concurrent in-flight checkpoints
     * @param externalizedCheckpointCleanup cleanup behavior for externalized checkpoints on cancellation
     * @param unalignedCheckpoints          whether unaligned checkpoints are enabled
     * @param alignedCheckpointTimeoutMs    timeout before switching from aligned to unaligned checkpoint
     * @param storageUri                    checkpoint storage directory URI (e.g. {@code "file:///checkpoints"} or {@code "s3://bucket/checkpoints"})
     */
    @JsonCreator
    public CheckpointingProperties(
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

    /**
     * Returns whether checkpointing is explicitly enabled.
     *
     * @return an {@link Optional} containing the enabled flag, or empty if not specified
     */
    public Optional<Boolean> enabled() {
        return Optional.ofNullable(enabled);
    }

    /**
     * Returns the optional checkpoint interval in milliseconds.
     *
     * @return an {@link OptionalLong} containing the interval in milliseconds, or empty if not specified
     */
    public OptionalLong intervalMs() {
        if (intervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(intervalMs);
    }

    /**
     * Returns the optional checkpointing consistency mode.
     *
     * @return an {@link Optional} containing the {@link CheckpointingMode}, or empty if not specified
     */
    public Optional<CheckpointingMode> mode() {
        return Optional.ofNullable(mode);
    }

    /**
     * Returns the optional checkpoint timeout in milliseconds.
     *
     * @return an {@link OptionalLong} containing the timeout in milliseconds, or empty if not specified
     */
    public OptionalLong timeoutMs() {
        if (timeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(timeoutMs);
    }

    /**
     * Returns the optional minimum pause duration between consecutive checkpoints in milliseconds.
     *
     * @return an {@link OptionalLong} containing the pause in milliseconds, or empty if not specified
     */
    public OptionalLong minPauseBetweenCheckpointsMs() {
        if (minPauseBetweenCheckpointsMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(minPauseBetweenCheckpointsMs);
    }

    /**
     * Returns the optional maximum number of concurrent checkpoints.
     *
     * @return an {@link OptionalInt} containing the maximum concurrent checkpoints, or empty if not specified
     */
    public OptionalInt maxConcurrentCheckpoints() {
        if (maxConcurrentCheckpoints == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxConcurrentCheckpoints);
    }

    /**
     * Returns the optional cleanup mode for externalized checkpoints upon cancellation.
     *
     * @return an {@link Optional} containing the {@link ExternalizedCheckpointCleanupMode}, or empty if not specified
     */
    public Optional<ExternalizedCheckpointCleanupMode> externalizedCheckpointCleanup() {
        return Optional.ofNullable(externalizedCheckpointCleanup);
    }

    /**
     * Returns whether unaligned checkpoints are enabled.
     *
     * @return an {@link Optional} containing the unaligned checkpoints flag, or empty if not specified
     */
    public Optional<Boolean> unalignedCheckpoints() {
        return Optional.ofNullable(unalignedCheckpoints);
    }

    /**
     * Returns the optional alignment timeout in milliseconds before falling back to unaligned checkpointing.
     *
     * @return an {@link OptionalLong} containing the timeout in milliseconds, or empty if not specified
     */
    public OptionalLong alignedCheckpointTimeoutMs() {
        if (alignedCheckpointTimeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(alignedCheckpointTimeoutMs);
    }

    /**
     * Returns the optional checkpoint storage directory URI.
     *
     * @return an {@link Optional} containing the storage URI string, or empty if not specified
     */
    public Optional<String> storageUri() {
        return Optional.ofNullable(storageUri);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof CheckpointingProperties)) {
            return false;
        }
        var o = (CheckpointingProperties) other;
        return Objects.equals(enabled, o.enabled)
            && Objects.equals(intervalMs, o.intervalMs)
            && mode == o.mode
            && Objects.equals(timeoutMs, o.timeoutMs)
            && Objects.equals(minPauseBetweenCheckpointsMs, o.minPauseBetweenCheckpointsMs)
            && Objects.equals(maxConcurrentCheckpoints, o.maxConcurrentCheckpoints)
            && externalizedCheckpointCleanup == o.externalizedCheckpointCleanup
            && Objects.equals(unalignedCheckpoints, o.unalignedCheckpoints)
            && Objects.equals(alignedCheckpointTimeoutMs, o.alignedCheckpointTimeoutMs)
            && Objects.equals(storageUri, o.storageUri);
    }

    @Override
    @Generated
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

    @Override
    @Generated
    public String toString() {
        return "CheckpointingProperties{" +
            "enabled=" + enabled +
            ", intervalMs=" + intervalMs +
            ", mode=" + mode +
            ", timeoutMs=" + timeoutMs +
            ", minPauseBetweenCheckpointsMs=" + minPauseBetweenCheckpointsMs +
            ", maxConcurrentCheckpoints=" + maxConcurrentCheckpoints +
            ", externalizedCheckpointCleanup=" + externalizedCheckpointCleanup +
            ", unalignedCheckpoints=" + unalignedCheckpoints +
            ", alignedCheckpointTimeoutMs=" + alignedCheckpointTimeoutMs +
            ", storageUri='" + storageUri + '\'' +
            '}';
    }
}
