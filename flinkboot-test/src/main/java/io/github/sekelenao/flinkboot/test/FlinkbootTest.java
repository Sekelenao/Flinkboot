package io.github.sekelenao.flinkboot.test;

import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

public final class FlinkbootTest {

    private FlinkbootTest() {
       throw new AssertionError("You cannot instantiate this class");
    }

    public static void assertPojo(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class to assert must not be null");
        var typeInfo = TypeExtractor.createTypeInfo(clazz);
        Assertions.assertInstanceOf(PojoTypeInfo.class, typeInfo, () -> String.format(
            "Class '%s' is not recognized as a POJO by Apache Flink's TypeExtractor. " +
            "Ensure it is public, has a public zero-argument constructor, and all fields are public or have public getters/setters.",
            clazz.getName()
        ));
    }
}
