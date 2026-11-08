package com.flinkboot.test.internal.pojo.handler;

import com.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;

import java.util.Objects;
import java.util.function.Consumer;

public final class GenericTypeHandler implements TypeInformationHandler {

    @Override
    public boolean supports(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "typeInfo must not be null");
        return typeInfo instanceof GenericTypeInfo;
    }

    @Override
    public void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer, Consumer<String> errors) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(enqueuer, "enqueuer must not be null");
        Objects.requireNonNull(errors, "errors must not be null");
        var genericTypeInfo = (GenericTypeInfo<?>) task.typeInfo();
        var typeName = genericTypeInfo.getTypeClass().getName();
        errors.accept(String.format(
            "Field or type '%s' is recognized as GenericTypeInfo (%s), which falls back to Kryo serialization.",
            task.path(),
            typeName
        ));
    }

}
