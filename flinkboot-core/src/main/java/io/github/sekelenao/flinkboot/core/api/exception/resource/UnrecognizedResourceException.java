package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class UnrecognizedResourceException extends FlinkbootException {

    private static final long serialVersionUID = 1L;
    public UnrecognizedResourceException(String location) {
        super("Location should start with either 'classpath:' or 'file:' but was: " + location);
    }
}
