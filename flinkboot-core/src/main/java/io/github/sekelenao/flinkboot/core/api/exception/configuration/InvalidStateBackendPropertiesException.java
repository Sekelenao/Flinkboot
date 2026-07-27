package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidStateBackendPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidStateBackendPropertiesException(String message) {
        super(message);
    }

    public InvalidStateBackendPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
