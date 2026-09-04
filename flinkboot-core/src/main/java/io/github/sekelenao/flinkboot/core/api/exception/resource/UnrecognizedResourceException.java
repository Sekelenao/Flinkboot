package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when a resource location does not match any recognized URI scheme prefix.
 */
public class UnrecognizedResourceException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code UnrecognizedResourceException} for the specified unrecognized location.
     *
     * @param location the unrecognized resource location string
     */
    public UnrecognizedResourceException(String location) {
        super("Location should start with 'classpath:', 'resource:', or 'file:' but was: " + location);
    }
}
