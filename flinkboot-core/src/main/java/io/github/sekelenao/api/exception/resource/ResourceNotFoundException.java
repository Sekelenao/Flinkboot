package io.github.sekelenao.api.exception.resource;

import io.github.sekelenao.api.exception.FlinkbootException;

public class ResourceNotFoundException extends FlinkbootException {

    public ResourceNotFoundException(String location) {
        super("Resource could not be found: " + location);
    }

}
