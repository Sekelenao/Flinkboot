package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when an error occurs while accessing or opening a resource.
 */
public class ResourceAccessException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ResourceAccessException} with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceAccessException(String message){
        super(message);
    }

    /**
     * Constructs a new {@code ResourceAccessException} for a given location and root cause.
     *
     * @param location the resource location string
     * @param cause    the underlying I/O cause
     */
    public ResourceAccessException(String location, Throwable cause) {
        super("Unable to access resource: " + location, cause);
    }
}