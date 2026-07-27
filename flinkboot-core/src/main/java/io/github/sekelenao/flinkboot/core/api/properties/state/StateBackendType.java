package io.github.sekelenao.flinkboot.core.api.properties.state;

public enum StateBackendType {
    HASHMAP,
    ROCKSDB,
    CHANGELOG,
    CUSTOM
}
