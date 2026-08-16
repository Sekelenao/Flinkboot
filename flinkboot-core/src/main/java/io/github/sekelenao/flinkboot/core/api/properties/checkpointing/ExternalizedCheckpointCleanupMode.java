package io.github.sekelenao.flinkboot.core.api.properties.checkpointing;

/**
 * Cleanup mode for externalized checkpoints upon job cancellation.
 */
public enum ExternalizedCheckpointCleanupMode {
    /**
     * Retains externalized checkpoints on disk/storage when the job is cancelled.
     */
    RETAIN_ON_CANCELLATION,

    /**
     * Deletes externalized checkpoints when the job is cancelled.
     */
    DELETE_ON_CANCELLATION,

    /**
     * Disables externalized checkpoint persistence on cancellation.
     */
    NO_EXTERNALIZED_CHECKPOINTS
}
