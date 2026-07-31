package io.github.sekelenao.flinkboot.kafka.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidKafkaSourcePropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidKafkaSourcePropertiesException(String message) {
        super(message);
    }

}
