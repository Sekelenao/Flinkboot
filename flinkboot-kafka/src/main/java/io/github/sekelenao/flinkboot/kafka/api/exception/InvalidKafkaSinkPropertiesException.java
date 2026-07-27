package io.github.sekelenao.flinkboot.kafka.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidKafkaSinkPropertiesException extends FlinkbootException {

    public InvalidKafkaSinkPropertiesException(String message) {
        super(message);
    }

}
