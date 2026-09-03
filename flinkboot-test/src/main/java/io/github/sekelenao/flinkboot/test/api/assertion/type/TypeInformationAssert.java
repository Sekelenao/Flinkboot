package io.github.sekelenao.flinkboot.test.api.assertion.type;

import io.github.sekelenao.flinkboot.test.internal.PojoValidator;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.util.Objects;

/**
 * Fluent assertion provider for verifying Apache Flink serialization rules on a type described by a
 * {@link TypeInformation} or a {@link TypeHint}.
 * <p>
 * Unlike {@link ClassAssert}, this assertion keeps the generic parameters a {@link Class} literal would
 * erase, so it can target generic and composite types such as {@code Map<String, List<UserActivity>>}.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * FlinkbootAssertions.assertThat(new TypeHint<Map<String, List<UserActivity>>>() {})
 *     .isPojo();
 *
 * FlinkbootAssertions.assertThat(TypeInformation.of(UserActivity.class))
 *     .isPojo();
 * }</pre>
 *
 * @param <T> the described type
 */
public final class TypeInformationAssert<T> {

    private final TypeInformation<T> typeInfo;

    /**
     * Creates a new {@link TypeInformationAssert} for the given target {@link TypeInformation}.
     * <p>
     * Useful to validate a type description produced by a custom
     * {@link org.apache.flink.api.common.typeinfo.TypeInfoFactory} or by Flink itself.
     *
     * @param typeInfo the target type information to validate
     * @throws NullPointerException if {@code typeInfo} is {@code null}
     */
    public TypeInformationAssert(TypeInformation<T> typeInfo) {
        this.typeInfo = Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
    }

    /**
     * Creates a new {@link TypeInformationAssert} for the type captured by the given {@link TypeHint}.
     * <p>
     * Useful to validate generic types such as {@code new TypeHint<List<UserActivity>>() {}},
     * whose parameters would otherwise be erased.
     *
     * @param typeHint the type hint describing the target type to validate
     * @throws NullPointerException if {@code typeHint} is {@code null}
     */
    public TypeInformationAssert(TypeHint<T> typeHint) {
        Objects.requireNonNull(typeHint, "TypeHint to assert must not be null");
        this.typeInfo = TypeInformation.of(typeHint);
    }

    /**
     * Recursively verifies that the described type strictly adheres to Apache Flink's POJO serialization rules.
     * <p>
     * Validates public constructors, field visibility, getters/setters, nested types, generics,
     * collections, arrays, and {@code @TypeInfo} annotations to guarantee zero Kryo fallback.
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the type or any nested field violates Flink POJO rules
     */
    public TypeInformationAssert<T> isPojo() {
        new PojoValidator().validate(typeInfo);
        return this;
    }

}
