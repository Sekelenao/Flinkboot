package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.MapTypeInfo;

import java.util.Objects;
import java.util.function.Consumer;

public final class MapTypeHandler implements TypeInformationHandler {

    @Override
    public boolean supports(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "typeInfo must not be null");
        return typeInfo instanceof MapTypeInfo;
    }

    @Override
    public boolean isStructural() {
        return true;
    }

    @Override
    public void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(enqueuer, "enqueuer must not be null");
        var mapTypeInfo = (MapTypeInfo<?, ?>) task.typeInfo();
        enqueuer.accept(new PojoValidationTask<>(task.path() + "<key>", mapTypeInfo.getKeyTypeInfo()));
        enqueuer.accept(new PojoValidationTask<>(task.path() + "<value>", mapTypeInfo.getValueTypeInfo()));
    }

}
