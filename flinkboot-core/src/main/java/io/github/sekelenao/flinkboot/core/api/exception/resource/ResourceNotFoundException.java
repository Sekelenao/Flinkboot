package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when a requested resource cannot be located.
 */
public class ResourceNotFoundException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ResourceNotFoundException} for the specified missing location.
     *
     * @param location the missing resource location string
     */
    public ResourceNotFoundException(String location) {
        super("Resource could not be found: " + location);
    }

}
