package io.github.sekelenao.flinkboot.test.api.assertion.type;

import io.github.sekelenao.flinkboot.test.internal.PojoValidator;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.util.Objects;

/**
 * Fluent assertion provider for verifying Apache Flink serialization rules and class structures.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * FlinkbootAssertions.assertThat(UserActivity.class)
 *     .isPojo();
 *
 * FlinkbootAssertions.assertThat(new TypeHint<Map<String, UserActivity>>() {})
 *     .isPojo();
 * }</pre>
 */
public final class ClassAssert {

    private final TypeInformation<?> typeInfo;

    /**
     * Creates a new {@link ClassAssert} for the given target class.
     * <p>
     * The {@link TypeInformation} is extracted from the class itself, so any generic parameter
     * is erased. Use {@link #ClassAssert(TypeHint)} to keep generic information.
     *
     * @param type the target class to validate
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public ClassAssert(Class<?> type) {
        Objects.requireNonNull(type, "Class to assert must not be null");
        this.typeInfo = TypeExtractor.createTypeInfo(type);
    }

    /**
     * Creates a new {@link ClassAssert} for the type captured by the given {@link TypeHint}.
     * <p>
     * Useful to validate generic types such as {@code new TypeHint<List<UserActivity>>() {}},
     * whose parameters would otherwise be erased.
     *
     * @param typeHint the type hint describing the target type to validate
     * @throws NullPointerException if {@code typeHint} is {@code null}
     */
    public ClassAssert(TypeHint<?> typeHint) {
        Objects.requireNonNull(typeHint, "TypeHint to assert must not be null");
        this.typeInfo = TypeInformation.of(typeHint);
    }

    /**
     * Creates a new {@link ClassAssert} for the given target {@link TypeInformation}.
     * <p>
     * Useful to validate a type description produced by a custom
     * {@link org.apache.flink.api.common.typeinfo.TypeInfoFactory} or by Flink itself.
     *
     * @param typeInfo the target type information to validate
     * @throws NullPointerException if {@code typeInfo} is {@code null}
     */
    public ClassAssert(TypeInformation<?> typeInfo) {
        this.typeInfo = Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
    }

    /**
     * Recursively verifies that the target type strictly adheres to Apache Flink's POJO serialization rules.
     * <p>
     * Validates public constructors, field visibility, getters/setters, nested types, generics,
     * collections, arrays, and {@code @TypeInfo} annotations to guarantee zero Kryo fallback.
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the type or any nested field violates Flink POJO rules
     */
    public ClassAssert isPojo() {
        new PojoValidator().validate(typeInfo);
        return this;
    }

}
