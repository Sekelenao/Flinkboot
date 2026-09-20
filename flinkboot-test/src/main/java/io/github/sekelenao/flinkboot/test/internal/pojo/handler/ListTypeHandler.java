package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ListTypeInfo;

import java.util.Objects;
import java.util.function.Consumer;

public final class ListTypeHandler implements TypeInformationHandler {

    @Override
    public boolean supports(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "typeInfo must not be null");
        return typeInfo instanceof ListTypeInfo;
    }

    @Override
    public boolean isStructural() {
        return true;
    }

    @Override
    public void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(enqueuer, "enqueuer must not be null");
        var listTypeInfo = (ListTypeInfo<?>) task.typeInfo();
        enqueuer.accept(new PojoValidationTask<>(task.path() + "[]", listTypeInfo.getElementTypeInfo()));
    }

}
