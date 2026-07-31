package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class ResourceAccessException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public ResourceAccessException(String message){
        super(message);
    }

    public ResourceAccessException(String location, Throwable cause) {
        super("Unable to access resource: " + location, cause);
    }
}