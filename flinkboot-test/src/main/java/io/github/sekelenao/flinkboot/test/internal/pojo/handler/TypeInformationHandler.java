package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.util.function.Consumer;

public interface TypeInformationHandler {

    boolean supports(TypeInformation<?> typeInfo);

    default boolean isStructural() {
        return false;
    }

    void handle(PojoValidationTask<?> task, Consumer<PojoValidationTask<?>> enqueuer);

}
