package io.github.sekelenao.flinkboot.test.internal;

import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayDeque;
import java.util.Objects;

public final class PojoValidator {

    private final ArrayDeque<PojoValidationTask<?>> tasks = new ArrayDeque<>();

    public void validate(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        var typeInfo = TypeExtractor.createTypeInfo(clazz);
        Assertions.assertInstanceOf(PojoTypeInfo.class, typeInfo, () -> String.format(
            "Class '%s' is not recognized as a POJO by Apache Flink's TypeExtractor. " +
            "Ensure it is public, has a public zero-argument constructor, and all fields are public or have public getters/setters.",
            clazz.getName()
        ));
        tasks.add(new PojoValidationTask<>(clazz.getName(), typeInfo));
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
        } else if (typeInfo instanceof PojoTypeInfo) {
            var pojoTypeInfo = (PojoTypeInfo<?>) typeInfo;
            for (int i = 0; i < pojoTypeInfo.getArity(); i++) {
                var pojoField = pojoTypeInfo.getPojoFieldAt(i);
                var fieldPath = task.path() + "." + pojoField.getField().getName();
                tasks.add(new PojoValidationTask<>(fieldPath, pojoField.getTypeInformation()));
            }
        }
    }
}
