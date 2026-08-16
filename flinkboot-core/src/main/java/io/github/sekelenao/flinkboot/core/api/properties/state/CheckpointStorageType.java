package io.github.sekelenao.flinkboot.core.api.properties.state;

/**
 * Storage location type for state checkpoints.
 */
public enum CheckpointStorageType {
    /**
     * Store checkpoint metadata/state in JobManager heap memory.
     */
    JOBMANAGER,

    /**
     * Store checkpoint metadata/state in a durable filesystem or object store (POSIX, S3, HDFS, etc.).
     */
    FILESYSTEM
}
