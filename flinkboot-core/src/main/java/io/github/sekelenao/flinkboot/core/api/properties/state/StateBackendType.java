package io.github.sekelenao.flinkboot.core.api.properties.state;

/**
 * State backend implementation type.
 */
public enum StateBackendType {
    /**
     * In-memory Heap / HashMap state backend.
     */
    HASHMAP,

    /**
     * Embedded RocksDB state backend for large, out-of-core state.
     */
    ROCKSDB,

    /**
     * Changelog state backend (durable short-interval checkpointing).
     */
    CHANGELOG,

    /**
     * Custom state backend loaded via factory class name.
     */
    CUSTOM
}
