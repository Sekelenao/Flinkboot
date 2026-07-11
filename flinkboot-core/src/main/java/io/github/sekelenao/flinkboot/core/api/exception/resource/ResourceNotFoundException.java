package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class ResourceNotFoundException extends FlinkbootException {

    public ResourceNotFoundException(String location) {
        super("Resource could not be found: " + location);
    }

}
