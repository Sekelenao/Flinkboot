package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidStateBackendConfigurationException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidStateBackendConfigurationException(String message) {
        super(message);
    }

    public InvalidStateBackendConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
