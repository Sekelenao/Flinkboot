package com.flinkboot.core.api.properties.savepoint;

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
    NO_CLAIM
}
