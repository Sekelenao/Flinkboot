package io.github.sekelenao.flinkboot.kafka.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when Kafka source configuration properties are invalid or inconsistent.
 */
public class InvalidKafkaSourcePropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidKafkaSourcePropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidKafkaSourcePropertiesException(String message) {
        super(message);
    }

}
