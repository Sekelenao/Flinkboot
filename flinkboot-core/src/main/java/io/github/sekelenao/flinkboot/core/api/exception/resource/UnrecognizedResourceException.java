package io.github.sekelenao.flinkboot.core.api.exception.resource;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class UnrecognizedResourceException extends FlinkbootException {
    public UnrecognizedResourceException(String location) {
        super("Location should start with either 'classpath:' or 'file:' but was: " + location);
    }
}
