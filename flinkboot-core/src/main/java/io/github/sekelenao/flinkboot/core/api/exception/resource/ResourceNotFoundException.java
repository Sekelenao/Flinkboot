package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class ResourceNotFoundException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String location) {
        super("Resource could not be found: " + location);
    }

}
