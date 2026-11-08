package com.flinkboot.test.internal.pojo;

import com.flinkboot.test.internal.pojo.handler.CompositeTypeHandler;
import com.flinkboot.test.internal.pojo.handler.EitherTypeHandler;
import com.flinkboot.test.internal.pojo.handler.GenericTypeHandler;
import com.flinkboot.test.internal.pojo.handler.ListTypeHandler;
import com.flinkboot.test.internal.pojo.handler.MapTypeHandler;
import com.flinkboot.test.internal.pojo.handler.MissingTypeHandler;
import com.flinkboot.test.internal.pojo.handler.ObjectArrayTypeHandler;
import com.flinkboot.test.internal.pojo.handler.PojoTypeHandler;
import com.flinkboot.test.internal.pojo.handler.TypeInformationHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

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

    private void processTask(PojoValidationTask<?> task, Consumer<String> errors) {
        var typeInfo = task.typeInfo();
        for (var handler : HANDLERS) {
            if (handler.supports(typeInfo)) {
                if (handler.isStructural() && !visited.add(typeInfo)) {
                    return;
                }
                handler.handle(task, tasks::add, errors);
                return;
            }
        }
    }

    public void validate(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
        var errors = new ArrayList<String>();
        tasks.add(new PojoValidationTask<>("$", typeInfo));
        while (!tasks.isEmpty()) {
            processTask(tasks.pop(), errors::add);
        }
        if (!errors.isEmpty()) {
            var message = new StringBuilder("Found ")
                .append(errors.size())
                .append(" POJO validation violation(s):");
            for (var error : errors) {
                message.append("\n- ").append(error);
            }
            throw new AssertionFailedError(message.toString());
        }
    }

}
