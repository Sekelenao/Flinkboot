package io.github.sekelenao.flinkboot.test.api;

import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.test.internal.PojoValidator;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public final class FlinkbootTest {

    private FlinkbootTest() {
       throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Reads, parses, and validates a configuration targeting the specified resource locations.
     * Each path must explicitly include a resource scheme prefix (e.g. {@code "classpath:app.yaml"} or {@code "file:/tmp/app.yaml"}).
     *
     * @param configurationClass the target configuration class
     * @param paths varargs of configuration resource locations
     * @param <C> type of the configuration
     * @return the deserialized and validated configuration object
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

    public static void assertPojo(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class to assert must not be null");
        new PojoValidator().validate(clazz);
    }
}
