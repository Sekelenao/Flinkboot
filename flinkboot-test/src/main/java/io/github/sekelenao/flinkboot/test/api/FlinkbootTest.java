package io.github.sekelenao.flinkboot.test.api;

import io.github.sekelenao.flinkboot.core.api.Flinkboot;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Testing utility providing helpers for loading and resolving configurations in tests.
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
        Objects.requireNonNull(configurationClass, "configurationClass must not be null");
        Objects.requireNonNull(paths, "paths must not be null");
        var args = new String[0];
        if (paths.length > 0) {
            args = new String[]{"-flinkboot-configurations", String.join(",", paths)};
        }
        try {
            return Flinkboot.initialize(args).configuration(configurationClass);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

}

