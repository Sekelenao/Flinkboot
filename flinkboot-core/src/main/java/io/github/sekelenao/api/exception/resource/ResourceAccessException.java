package io.github.sekelenao.api.exception.resource;

import io.github.sekelenao.api.exception.FlinkbootException;

public class ResourceAccessException extends FlinkbootException {

    public ResourceAccessException(String message){
        super(message);
    }

    public ResourceAccessException(String location, Throwable cause) {
        super("Unable to access resource: " + location, cause);
    }
}