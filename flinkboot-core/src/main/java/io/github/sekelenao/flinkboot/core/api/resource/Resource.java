package io.github.sekelenao.flinkboot.core.api.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceAccessException;
import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import io.github.sekelenao.flinkboot.core.api.exception.resource.UnrecognizedResourceException;
import io.github.sekelenao.flinkboot.core.internal.resource.ClasspathResource;
import io.github.sekelenao.flinkboot.core.internal.resource.FileSystemResource;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * Unified abstraction for reading configuration files and assets across heterogeneous storage locations.
 * <p>
 * Supported URI schemes:
 * <ul>
 *   <li>{@code classpath:<path>} or {@code resource:<path>} — Loads a resource from the application JAR classpath.</li>
 *   <li>{@code file:<path>} — Loads a file from the local or shared filesystem.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * try (InputStream stream = Resource.of("classpath:job-configuration.yaml").inputStream()) {
 *     // Process resource stream
 * }
 * }</pre>
 */
public interface Resource {

    /**
     * Resolves a {@code Resource} instance based on the provided location URI string.
     *
     * @param location the location URI (e.g. {@code "classpath:config.yaml"} or {@code "file:/etc/job.yaml"})
     * @return a {@code Resource} instance targeting the location
     * @throws NullPointerException           if {@code location} is {@code null}
     * @throws UnrecognizedResourceException   if {@code location} lacks a valid scheme prefix or has an empty path
     */
    static Resource of(String location) {
        Objects.requireNonNull(location);
        var trimmedLocation = location.strip();
        var index = trimmedLocation.indexOf(':');
        if (index <= 0) {
            throw new UnrecognizedResourceException(location);
        }
        var prefix = trimmedLocation.substring(0, index).toLowerCase(Locale.ROOT);
        var suffix = trimmedLocation.substring(index + 1).strip();
        if (suffix.isEmpty()) {
            throw new UnrecognizedResourceException(location);
        }
        if (prefix.equals("classpath") || prefix.equals("resource")) {
            return new ClasspathResource(suffix);
        }
        if (prefix.equals("file")) {
            return new FileSystemResource(suffix);
        }
        throw new UnrecognizedResourceException(location);
    }

    /**
     * Opens a new {@link InputStream} for reading the resource contents.
     * <p>
     * Callers are responsible for closing the returned stream.
     *
     * @return a non-null {@link InputStream} to the resource
     * @throws ResourceNotFoundException if the target resource does not exist
     * @throws ResourceAccessException   if the resource cannot be accessed
     */
    InputStream inputStream();

}
