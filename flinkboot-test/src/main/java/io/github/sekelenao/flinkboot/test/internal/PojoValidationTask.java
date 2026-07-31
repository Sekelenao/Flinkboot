package io.github.sekelenao.flinkboot.test.internal;

import org.apache.flink.api.common.typeinfo.TypeInformation;

public final class PojoValidationTask<T> {

    private final String path;
    private final TypeInformation<T> typeInfo;

    public PojoValidationTask(String path, TypeInformation<T> typeInfo) {
        this.path = path;
        this.typeInfo = typeInfo;
    }

    public String path() {
        return path;
    }

    public TypeInformation<T> typeInfo() {
        return typeInfo;
    }

}
