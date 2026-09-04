package io.github.sekelenao.flinkboot.test.api.assertion;

import io.github.sekelenao.flinkboot.test.api.assertion.type.ClassAssert;
import io.github.sekelenao.flinkboot.test.api.assertion.type.TypeInformationAssert;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.util.Objects;


/**
 * Entry point for Flinkboot fluent assertions.
 * <p>
 * Provides factory methods to instantiate dedicated assert objects for validating
 * Apache Flink POJO serialization compliance and data structures.
 *
 * <h3>Example: Asserting POJO Compliance</h3>
 * <pre>{@code
 * @Test
 * void shouldBePojoCompliant() {
 *     FlinkbootAssertions.assertThat(UserActivity.class)
 *         .isPojo();
 *
 *     FlinkbootAssertions.assertThat(new TypeHint<Map<String, UserActivity>>() {})
 *         .isPojo();
 * }
 * }</pre>
 */
public final class FlinkbootAssertions {

    private FlinkbootAssertions() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates a new instance of {@link ClassAssert} to assert on the specified class.
     *
     * @param type the target class to assert
     * @return the created {@link ClassAssert} assertion object
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ClassAssert assertThat(Class<?> type) {
        Objects.requireNonNull(type, "Class to assert must not be null");
        return new ClassAssert(type);
    }

    /**
     * Creates a new instance of {@link TypeInformationAssert} to assert on the type captured by the
     * specified {@link TypeHint}.
     * <p>
     * Use it to keep generic parameters that a {@link Class} literal would erase, such as
     * {@code new TypeHint<List<UserActivity>>() {}}.
     *
     * @param typeHint the type hint describing the target type to assert
     * @param <T> the type described by the hint
     * @return the created {@link TypeInformationAssert} assertion object
     * @throws NullPointerException if {@code typeHint} is {@code null}
     */
    public static <T> TypeInformationAssert<T> assertThat(TypeHint<T> typeHint) {
        Objects.requireNonNull(typeHint, "TypeHint to assert must not be null");
        return new TypeInformationAssert<>(typeHint);
    }

    /**
     * Creates a new instance of {@link TypeInformationAssert} to assert on the specified
     * {@link TypeInformation}.
     * <p>
     * Use it to assert on a type description produced by a custom
     * {@link org.apache.flink.api.common.typeinfo.TypeInfoFactory} or by Flink itself.
     *
     * @param typeInfo the target type information to assert
     * @param <T> the described type
     * @return the created {@link TypeInformationAssert} assertion object
     * @throws NullPointerException if {@code typeInfo} is {@code null}
     */
    public static <T> TypeInformationAssert<T> assertThat(TypeInformation<T> typeInfo) {
        Objects.requireNonNull(typeInfo, "TypeInformation to assert must not be null");
        return new TypeInformationAssert<>(typeInfo);
    }
}
