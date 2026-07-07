package io.github.sekelenao.api.exception.resource;

import io.github.sekelenao.api.exception.FlinkbootException;

public class UnrecognizedResourceException extends FlinkbootException {
    public UnrecognizedResourceException(String location) {
        super("Location should start with either 'classpath:' or 'file:' but was: " + location);
    }
}
