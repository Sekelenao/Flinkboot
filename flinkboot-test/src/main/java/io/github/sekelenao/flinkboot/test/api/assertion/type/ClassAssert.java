package io.github.sekelenao.flinkboot.test.api.assertion.type;

import io.github.sekelenao.flinkboot.test.internal.PojoValidator;

import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.util.Objects;

/**
 * Fluent assertion provider for verifying Apache Flink serialization rules and class structures.
 * <p>
 * This assertion targets {@link Class} literals only, so any generic parameter is erased. Use
 * {@link TypeInformationAssert} to assert on generic or composite types described by a
 * {@link org.apache.flink.api.common.typeinfo.TypeHint} or a
 * {@link org.apache.flink.api.common.typeinfo.TypeInformation}.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * FlinkbootAssertions.assertThat(UserActivity.class)
 *     .isPojo();
 * }</pre>
 */
public final class ClassAssert {

    private final Class<?> type;

    /**
     * Creates a new {@link ClassAssert} for the given target class.
     *
     * @param type the target class to validate
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public ClassAssert(Class<?> type) {
        this.type = Objects.requireNonNull(type, "Class to assert must not be null");
    }

    /**
     * Recursively verifies that the target class strictly adheres to Apache Flink's POJO serialization rules.
     * <p>
     * Validates public constructors, field visibility, getters/setters, nested types, generics,
     * collections, arrays, and {@code @TypeInfo} annotations to guarantee zero Kryo fallback.
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the class or any nested field violates Flink POJO rules
     */
    public ClassAssert isPojo() {
        new PojoValidator().validate(TypeExtractor.createTypeInfo(type));
        return this;
    }

}
