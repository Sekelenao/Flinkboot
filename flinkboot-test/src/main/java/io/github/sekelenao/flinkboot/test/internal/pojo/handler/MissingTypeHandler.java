package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.MissingTypeInfo;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;
import java.util.function.Consumer;

public final class MissingTypeHandler implements TypeInformationHandler {

    @Override
    public boolean supports(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "typeInfo must not be null");
        return typeInfo instanceof MissingTypeInfo;
    }

    @Override
    public void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(enqueuer, "enqueuer must not be null");
        var missing = (MissingTypeInfo) task.typeInfo();
        var reason = "unknown type erasure";
        if (missing.getTypeException() != null && missing.getTypeException().getMessage() != null) {
            reason = missing.getTypeException().getMessage();
        }
        Assertions.fail(String.format(
            "Field or type '%s' has missing type information (%s) and cannot be serialized by Flink.",
            task.path(),
            reason
        ));
    }

}
