package io.github.sekelenao.flinkboot.core.internal.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class YamlParser implements AutoCloseable {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private final YAMLMapper mapper;

    public YamlParser() {
        this(additionalConfiguration -> {});
    }

    public YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration) {
        Objects.requireNonNull(additionalConfiguration);
        var builder = YAMLMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndAddModules();
        additionalConfiguration.accept(builder);
        this.mapper = builder.build();
    }

    public YamlParser(YAMLMapper mapper){
        this.mapper = Objects.requireNonNull(mapper);
    }

    public <Y> Y parse(InputStream source, Class<Y> clazz) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(clazz);
        try {
            var yaml = mapper.readValue(source, clazz);
            if (yaml == null) {
                throw new YamlParsingException("Parsing resulted to null for configuration class: " + clazz.getName());
            }
            var violations = validatorFactory.getValidator().validate(yaml);
            if(!violations.isEmpty()){
                var message = violations.stream().limit(3)
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
                throw new ConfigurationValidationException(message);
            }
            return yaml;
        } catch (IOException exception) {
            throw new YamlParsingException(exception.getMessage(), exception);
        }
    }

    @Override
    public void close() {
        validatorFactory.close();
    }

}
