package io.github.sekelenao.api.exception.resource;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String location) {
        super("Resource could not be found: " + location);
    }

}
