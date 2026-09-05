package io.github.sekelenao.flinkboot.test.internal;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import java.util.Objects;

public final class PojoValidationTask<T> {

    private final String path;
    private final TypeInformation<T> typeInfo;

    public PojoValidationTask(String path, TypeInformation<T> typeInfo) {
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.typeInfo = Objects.requireNonNull(typeInfo, "typeInfo must not be null");
    }

    public String path() {
        return path;
    }

    public TypeInformation<T> typeInfo() {
        return typeInfo;
    }

}
