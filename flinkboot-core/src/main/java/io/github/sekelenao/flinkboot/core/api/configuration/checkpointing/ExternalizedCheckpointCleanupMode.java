package io.github.sekelenao.flinkboot.core.api.configuration.checkpointing;

public enum ExternalizedCheckpointCleanupMode {
    RETAIN_ON_CANCELLATION,
    DELETE_ON_CANCELLATION,
    NO_EXTERNALIZED_CHECKPOINTS
}
