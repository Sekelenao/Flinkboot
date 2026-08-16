package io.github.sekelenao.flinkboot.test.api;

import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.test.internal.PojoValidator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Testing utility providing helpers for verifying Flink POJO compliance and loading configurations in tests.
 *
 * <h3>Example: Asserting POJO Compliance</h3>
 * <pre>{@code
 * @Test
 * void shouldBePojoCompliant() {
 *     FlinkbootTest.assertPojo(MyEventDto.class);
 * }
 * }</pre>
 *
 * <h3>Example: Loading Configuration in Unit Tests</h3>
 * <pre>{@code
 * @Test
 * void shouldLoadTestConfiguration() {
 *     MyJobConfig config = FlinkbootTest.configuration(MyJobConfig.class, "classpath:test-config.yaml");
 *     assertEquals(4, config.job().environment().get().execution().get().parallelism().getAsInt());
 * }
 * }</pre>
 */
public final class FlinkbootTest {

    private FlinkbootTest() {
       throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Reads, parses, and validates a configuration targeting the specified resource locations.
     * <p>
     * Each path must explicitly include a resource scheme prefix (e.g. {@code "classpath:app.yaml"} or {@code "file:/tmp/app.yaml"}).
     *
     * @param configurationClass the target configuration class
     * @param paths              varargs of configuration resource locations
     * @param <C>                type of the configuration
     * @return the deserialized and validated configuration object
     * @throws UncheckedIOException if an I/O error occurs while reading configuration files
     * @throws NullPointerException if {@code configurationClass} or {@code paths} is {@code null}
     */
    public static <C> C configuration(Class<C> configurationClass, String... paths) {
        Objects.requireNonNull(configurationClass, "Configuration class must not be null");
        Objects.requireNonNull(paths, "Configuration paths must not be null");
        var joinedPaths = String.join(",", paths);
        try {
            return Flinkboot.initialize(new String[]{"-flinkboot-configurations", joinedPaths})
                .configuration(configurationClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Recursively verifies that a Java class strictly adheres to Apache Flink's POJO serialization rules.
     * <p>
     * Validates public constructors, field visibility, getters/setters, nested types, generics,
     * collections, arrays, and {@code @TypeInfo} annotations to guarantee zero Kryo fallback.
     *
     * @param clazz the class to validate for POJO compliance
     * @throws NullPointerException if {@code clazz} is {@code null}
     * @throws AssertionError       if the class or any nested field violates Flink POJO rules
     */
    public static void assertPojo(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class to assert must not be null");
        new PojoValidator().validate(clazz);
    }
}
