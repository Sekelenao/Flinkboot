package io.github.sekelenao.flinkboot.core.api.properties.savepoint;

/**
 * Savepoint restore ownership mode.
 */
public enum RestoreMode {
    /**
     * Flink takes ownership of the savepoint file and integrates it into subsequent checkpoints.
     */
    CLAIM,

    /**
     * Flink does not take ownership of the savepoint; original files remain intact and untouched.
     */
    NO_CLAIM,

    /**
     * Legacy restore mode behavior (Flink 1.14 and earlier).
     */
    LEGACY
}
