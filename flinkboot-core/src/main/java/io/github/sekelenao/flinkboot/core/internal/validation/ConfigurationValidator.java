package io.github.sekelenao.flinkboot.core.internal.validation;

import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ConfigurationValidator implements AutoCloseable {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private final Validator validator = factory.getValidator();

    private final int capacity;

    public ConfigurationValidator(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly positive");
        }
        this.capacity = capacity;
    }

    public <C> void validate(C configuration) {
        Objects.requireNonNull(configuration);
        var violations = validator.validate(configuration);
        if (violations.isEmpty()) {
            return;
        }
        var prefix = "Configuration validation failed with " + violations.size() + " violation(s):";
        var description = violations.stream()
            .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
            .limit(capacity)
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining("\n - ", "\n - ", "\n"));
        var message = prefix + description;
        var bufferOverflow = violations.size() - capacity;
        if (bufferOverflow > 0) {
            var suffix = " - ... and " + bufferOverflow + " more violation(s)";
            message += suffix;
        }
        throw new ConfigurationValidationException(message);
    }

    @Override
    public void close() {
        factory.close();
    }
}
