package com.flinkboot.core.api.properties.state;

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
     * Custom state backend loaded via factory class name.
     */
    CUSTOM
}
