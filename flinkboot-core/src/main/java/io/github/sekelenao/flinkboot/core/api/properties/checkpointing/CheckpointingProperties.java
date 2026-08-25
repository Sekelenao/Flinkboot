package io.github.sekelenao.flinkboot.core.api.properties.checkpointing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Configuration properties for Flink stream checkpointing.
 * <p>
 * Configures interval, timeout, consistency mode, concurrency, unaligned checkpoints,
 * externalized checkpoint cleanup, and storage URI.
 */
public final class CheckpointingProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Boolean enabled;

    private final Duration interval;

    private final CheckpointingMode mode;

    private final Duration timeout;

    private final Duration minPauseBetweenCheckpoints;

    @Positive
    private final Integer maxConcurrentCheckpoints;

    private final ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup;

    private final Boolean unalignedCheckpoints;

    private final Duration alignedCheckpointTimeout;

    private final String storageUri;

    /**
     * Creates a new {@code CheckpointingProperties} instance.
     *
     * @param enabled                       whether checkpointing is enabled
     * @param interval                      checkpoint interval duration
     * @param mode                          consistency mode (EXACTLY_ONCE or AT_LEAST_ONCE)
     * @param timeout                       checkpoint timeout duration
     * @param minPauseBetweenCheckpoints    minimum pause duration between consecutive checkpoints
     * @param maxConcurrentCheckpoints      maximum number of concurrent in-flight checkpoints
     * @param externalizedCheckpointCleanup cleanup behavior for externalized checkpoints on cancellation
     * @param unalignedCheckpoints          whether unaligned checkpoints are enabled
     * @param alignedCheckpointTimeout      timeout before switching from aligned to unaligned checkpoint
     * @param storageUri                    checkpoint storage directory URI (e.g. {@code "file:///checkpoints"} or {@code "s3://bucket/checkpoints"})
     */
    @JsonCreator
    public CheckpointingProperties(
        @JsonProperty("enabled") Boolean enabled,
        @JsonProperty("interval") Duration interval,
        @JsonProperty("mode") CheckpointingMode mode,
        @JsonProperty("timeout") Duration timeout,
        @JsonProperty("min-pause-between-checkpoints") Duration minPauseBetweenCheckpoints,
        @JsonProperty("max-concurrent-checkpoints") Integer maxConcurrentCheckpoints,
        @JsonProperty("externalized-checkpoint-cleanup") ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup,
        @JsonProperty("unaligned-checkpoints") Boolean unalignedCheckpoints,
        @JsonProperty("aligned-checkpoint-timeout") Duration alignedCheckpointTimeout,
        @JsonProperty("storage-uri") String storageUri
    ) {
        this.enabled = enabled;
        this.interval = interval;
        this.mode = mode;
        this.timeout = timeout;
        this.minPauseBetweenCheckpoints = minPauseBetweenCheckpoints;
        this.maxConcurrentCheckpoints = maxConcurrentCheckpoints;
        this.externalizedCheckpointCleanup = externalizedCheckpointCleanup;
        this.unalignedCheckpoints = unalignedCheckpoints;
        this.alignedCheckpointTimeout = alignedCheckpointTimeout;
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
     * Returns the optional checkpoint interval duration.
     *
     * @return an {@link Optional} containing the interval duration, or empty if not specified
     */
    public Optional<Duration> interval() {
        return Optional.ofNullable(interval);
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
     * Returns the optional checkpoint timeout duration.
     *
     * @return an {@link Optional} containing the timeout duration, or empty if not specified
     */
    public Optional<Duration> timeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Returns the optional minimum pause duration between consecutive checkpoints.
     *
     * @return an {@link Optional} containing the pause duration, or empty if not specified
     */
    public Optional<Duration> minPauseBetweenCheckpoints() {
        return Optional.ofNullable(minPauseBetweenCheckpoints);
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
     * Returns the optional alignment timeout duration before falling back to unaligned checkpointing.
     *
     * @return an {@link Optional} containing the timeout duration, or empty if not specified
     */
    public Optional<Duration> alignedCheckpointTimeout() {
        return Optional.ofNullable(alignedCheckpointTimeout);
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
            && Objects.equals(interval, o.interval)
            && mode == o.mode
            && Objects.equals(timeout, o.timeout)
            && Objects.equals(minPauseBetweenCheckpoints, o.minPauseBetweenCheckpoints)
            && Objects.equals(maxConcurrentCheckpoints, o.maxConcurrentCheckpoints)
            && externalizedCheckpointCleanup == o.externalizedCheckpointCleanup
            && Objects.equals(unalignedCheckpoints, o.unalignedCheckpoints)
            && Objects.equals(alignedCheckpointTimeout, o.alignedCheckpointTimeout)
            && Objects.equals(storageUri, o.storageUri);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(
            enabled,
            interval,
            mode,
            timeout,
            minPauseBetweenCheckpoints,
            maxConcurrentCheckpoints,
            externalizedCheckpointCleanup,
            unalignedCheckpoints,
            alignedCheckpointTimeout,
            storageUri
        );
    }

    @Override
    @Generated
    public String toString() {
        return "CheckpointingProperties{" +
            "enabled=" + enabled +
            ", interval=" + interval +
            ", mode=" + mode +
            ", timeout=" + timeout +
            ", minPauseBetweenCheckpoints=" + minPauseBetweenCheckpoints +
            ", maxConcurrentCheckpoints=" + maxConcurrentCheckpoints +
            ", externalizedCheckpointCleanup=" + externalizedCheckpointCleanup +
            ", unalignedCheckpoints=" + unalignedCheckpoints +
            ", alignedCheckpointTimeout=" + alignedCheckpointTimeout +
            ", storageUri='" + storageUri + '\'' +
            '}';
    }
}

