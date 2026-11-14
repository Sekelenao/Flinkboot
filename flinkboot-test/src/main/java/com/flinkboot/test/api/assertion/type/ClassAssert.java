package com.flinkboot.test.api.assertion.type;

import com.flinkboot.test.internal.pojo.PojoValidator;
import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.opentest4j.AssertionFailedError;

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
     * Recursively verifies that the target class strictly adheres to native Apache Flink serialization rules,
     * guaranteeing zero Kryo fallback across the entire type hierarchy.
     * <p>
     * Validates public constructors, field visibility, getters/setters, nested types, generics,
     * collections, maps, Tuples, arrays, and {@code @TypeInfo} annotations across the type tree.
     * Guarantees that neither {@link org.apache.flink.api.java.typeutils.GenericTypeInfo} nor
     * {@link org.apache.flink.api.java.typeutils.MissingTypeInfo} is present in the extracted type tree.
     * </p>
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the class or any nested field violates native Flink serialization rules
     */
    public ClassAssert isPojo() {
        try {
            new PojoValidator().validate(TypeExtractor.createTypeInfo(type));
            return this;
        } catch (InvalidTypesException exception) {
            var message = "Expected class <" + type.getName() + "> to be a valid POJO, but type extraction failed";
            throw new AssertionFailedError(message, exception);
        }
    }

}
