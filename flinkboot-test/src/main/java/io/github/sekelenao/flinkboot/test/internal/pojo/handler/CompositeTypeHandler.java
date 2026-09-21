package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.CompositeType;

import java.util.Objects;
import java.util.function.Consumer;

public final class CompositeTypeHandler implements TypeInformationHandler {

    @Override
    public boolean supports(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "typeInfo must not be null");
        return typeInfo instanceof CompositeType;
    }

    @Override
    public boolean isStructural() {
        return true;
    }

    @Override
    public void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(enqueuer, "enqueuer must not be null");
        var compositeType = (CompositeType<?>) task.typeInfo();
        for (int i = 0; i < compositeType.getArity(); i++) {
            enqueuer.accept(new PojoValidationTask<>(task.path() + "[" + i + "]", compositeType.getTypeAt(i)));
        }
    }

}
