package io.github.sekelenao.flinkboot.core.api.configuration.state;

public enum StateBackendType {
    HASHMAP,
    ROCKSDB,
    CHANGELOG,
    CUSTOM
}
