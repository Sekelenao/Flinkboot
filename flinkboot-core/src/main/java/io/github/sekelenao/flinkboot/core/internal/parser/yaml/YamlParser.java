package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private final ValidatorFactory validatorFactory;

    private final YAMLMapper mapper;

    private final JsonNode root;

    private final MergeProcessor mergeProcessor;

    public YamlParser(MergeFeatures features) {
        this(additionalConfiguration -> {}, Objects.requireNonNull(features));
    }

    public YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, MergeFeatures features) {
        Objects.requireNonNull(additionalConfiguration);
        Objects.requireNonNull(features);
        var builder = YAMLMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .findAndAddModules();
        additionalConfiguration.accept(builder);
        this.mapper = builder.build();
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, features);
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    public YamlParser(YAMLMapper mapper, MergeFeatures mergeFeatures){
        Objects.requireNonNull(mergeFeatures);
        this.mapper = Objects.requireNonNull(mapper);
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, mergeFeatures);
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    public void parse(InputStream source){
        Objects.requireNonNull(source);
        try {
            var node = mapper.readTree(source);
            if (node == null || node.isNull() || node.isMissingNode()) {
                return;
            }
            if (!node.isObject()) {
                throw new YamlParsingException("Configuration source is invalid");
            }
           mergeProcessor.apply((ObjectNode) node);
        } catch (IOException exception) {
            throw new YamlParsingException(exception.getMessage(), exception);
        }
    }

    public <Y> Y convertTo(Class<Y> type){
        Objects.requireNonNull(type);
        try {
            var yaml = mapper.treeToValue(root, type);
            if (yaml == null) {
                throw new YamlParsingException("Configuration could not be mapped to target class: " + type.getSimpleName());
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
