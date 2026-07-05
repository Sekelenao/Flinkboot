package io.github.sekelenao.internal.yaml;

import io.github.sekelenao.api.exception.configuration.ConfigurationException;
import io.github.sekelenao.api.exception.configuration.YamlParsingException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Collectors;

public final class YamlParser implements AutoCloseable {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private final YAMLMapper mapper = new YAMLMapper();

    public <Y> Y parse(InputStream source, Class<Y> clazz) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(clazz);
        try {
            var yaml = mapper.readValue(source, clazz);
            var violations = validatorFactory.getValidator().validate(yaml);
            if(!violations.isEmpty()){
                var message = violations.stream().limit(3)
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
                throw new ConfigurationException(message);
            }
            return yaml;
        } catch (JacksonException exception) {
            throw new YamlParsingException(exception.getMessage(), exception);
        }
    }

    @Override
    public void close() {
        validatorFactory.close();
    }

}
