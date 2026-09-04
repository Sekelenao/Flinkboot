package io.github.sekelenao.flinkboot.test.internal;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.CompositeType;
import org.apache.flink.api.java.typeutils.EitherTypeInfo;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.apache.flink.api.java.typeutils.ListTypeInfo;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class PojoValidator {

    private final ArrayDeque<PojoValidationTask<?>> tasks = new ArrayDeque<>();

    private final Set<TypeInformation<?>> visited = new HashSet<>();

    public void validate(TypeInformation<?> typeInfo) {
        Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
        tasks.add(new PojoValidationTask<>(typeInfo.getTypeClass().getName(), typeInfo));
        while (!tasks.isEmpty()) {
            processTask(tasks.pop());
        }
    }

    private void processTask(PojoValidationTask<?> task) {
        var typeInfo = task.typeInfo();
        if (typeInfo instanceof GenericTypeInfo) {
            var genericTypeInfo = (GenericTypeInfo<?>) typeInfo;
            Assertions.fail(String.format(
                "Field or type '%s' is recognized as GenericTypeInfo (%s), which falls back to Kryo serialization.",
                task.path(), genericTypeInfo.getTypeClass().getName()
            ));
        }

        if (!visited.add(typeInfo)) {
            return;
        }

        if (typeInfo instanceof PojoTypeInfo) {
            var pojoTypeInfo = (PojoTypeInfo<?>) typeInfo;
            for (int i = 0; i < pojoTypeInfo.getArity(); i++) {
                var pojoField = pojoTypeInfo.getPojoFieldAt(i);
                var fieldPath = task.path() + "." + pojoField.getField().getName();
                tasks.add(new PojoValidationTask<>(fieldPath, pojoField.getTypeInformation()));
            }
        } else if (typeInfo instanceof ListTypeInfo) {
            var listTypeInfo = (ListTypeInfo<?>) typeInfo;
            tasks.add(new PojoValidationTask<>(task.path() + "[]", listTypeInfo.getElementTypeInfo()));
        } else if (typeInfo instanceof MapTypeInfo) {
            var mapTypeInfo = (MapTypeInfo<?, ?>) typeInfo;
            tasks.add(new PojoValidationTask<>(task.path() + "<key>", mapTypeInfo.getKeyTypeInfo()));
            tasks.add(new PojoValidationTask<>(task.path() + "<value>", mapTypeInfo.getValueTypeInfo()));
        } else if (typeInfo instanceof ObjectArrayTypeInfo) {
            var arrayTypeInfo = (ObjectArrayTypeInfo<?, ?>) typeInfo;
            tasks.add(new PojoValidationTask<>(task.path() + "[]", arrayTypeInfo.getComponentInfo()));
        } else if (typeInfo instanceof EitherTypeInfo) {
            var eitherTypeInfo = (EitherTypeInfo<?, ?>) typeInfo;
            tasks.add(new PojoValidationTask<>(task.path() + "<left>", eitherTypeInfo.getLeftType()));
            tasks.add(new PojoValidationTask<>(task.path() + "<right>", eitherTypeInfo.getRightType()));
        } else if (typeInfo instanceof CompositeType) {
            var compositeType = (CompositeType<?>) typeInfo;
            for (int i = 0; i < compositeType.getArity(); i++) {
                tasks.add(new PojoValidationTask<>(task.path() + "[" + i + "]", compositeType.getTypeAt(i)));
            }
        }
    }
}
