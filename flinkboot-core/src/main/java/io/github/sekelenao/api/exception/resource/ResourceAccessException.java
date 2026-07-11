package io.github.sekelenao.api.exception.resource;

public class ResourceAccessException extends RuntimeException {

    public ResourceAccessException(String message){
        super(message);
    }

    public ResourceAccessException(String location, Throwable cause) {
        super("Unable to access resource: " + location, cause);
    }
}