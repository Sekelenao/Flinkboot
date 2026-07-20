package io.github.sekelenao.flinkboot.core.api.configuration.checkpointing;

public enum FlinkExternalizedCleanup {
    RETAIN_ON_CANCELLATION,
    DELETE_ON_CANCELLATION
}
