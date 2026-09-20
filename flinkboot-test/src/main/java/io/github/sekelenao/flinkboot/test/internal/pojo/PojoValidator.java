package io.github.sekelenao.flinkboot.test.internal.pojo;

import io.github.sekelenao.flinkboot.test.internal.pojo.handler.CompositeTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.EitherTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.GenericTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.ListTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.MapTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.MissingTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.ObjectArrayTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.PojoTypeHandler;
import io.github.sekelenao.flinkboot.test.internal.pojo.handler.TypeInformationHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PojoValidator {

    private static final List<TypeInformationHandler> HANDLERS = List.of(
        new MissingTypeHandler(),
        new GenericTypeHandler(),
        new PojoTypeHandler(),
        new ListTypeHandler(),
        new MapTypeHandler(),
        new ObjectArrayTypeHandler(),
        new EitherTypeHandler(),
        new CompositeTypeHandler()
    );

    private final ArrayDeque<PojoValidationTask<?>> tasks = new ArrayDeque<>();

    private final Set<TypeInformation<?>> visited = new HashSet<>();

    private void processTask(PojoValidationTask<?> task) {
        var typeInfo = task.typeInfo();
        for (var handler : HANDLERS) {
            if (handler.supports(typeInfo)) {
                if (handler.isStructural() && !visited.add(typeInfo)) {
                    return;
                }
                handler.handle(task, tasks::add);
                return;
            }
        }
    }

    public void validate(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
        tasks.add(new PojoValidationTask<>("$", typeInfo));
        while (!tasks.isEmpty()) {
            processTask(tasks.pop());
        }
    }

}
