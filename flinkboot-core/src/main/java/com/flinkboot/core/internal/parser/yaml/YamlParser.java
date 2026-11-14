package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flinkboot.core.api.exception.parsing.YamlParsingException;
import com.flinkboot.core.internal.annotation.VisibleForTesting;
import com.flinkboot.core.internal.startup.EnvVarResolver;
import com.flinkboot.core.internal.validation.ConfigurationValidator;
import com.flinkboot.core.internal.validation.ValidationFeatures;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

public final class YamlParser implements AutoCloseable {

    private final ConfigurationValidator validator;

    private final YAMLMapper mapper;

    private final JsonNode root;

    private final MergeProcessor mergeProcessor;

    public YamlParser(ParserFeatures parserFeatures, ValidationFeatures validationFeatures) {
        this(additionalConfiguration -> {}, Objects.requireNonNull(parserFeatures), Objects.requireNonNull(validationFeatures));
    }

    public YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures parserFeatures, ValidationFeatures validationFeatures) {
        this(additionalConfiguration, parserFeatures, validationFeatures, new PlaceholderResolver(new EnvVarResolver(System::getenv)));
    }

    @VisibleForTesting
    YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures parserFeatures, ValidationFeatures validationFeatures, PlaceholderResolver placeholderResolver) {
        Objects.requireNonNull(additionalConfiguration);
        Objects.requireNonNull(parserFeatures);
        Objects.requireNonNull(validationFeatures);
        Objects.requireNonNull(placeholderResolver);
        var builder = YAMLMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
            .addModule(new JavaTimeModule())
            .findAndAddModules();
        additionalConfiguration.accept(builder);
        this.mapper = builder.build();
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, parserFeatures, placeholderResolver);
        this.validator = ConfigurationValidator.from(validationFeatures);
    }

    public YamlParser(YAMLMapper mapper, ParserFeatures parserFeatures, ValidationFeatures validationFeatures){
        this(mapper, parserFeatures, validationFeatures, new PlaceholderResolver(new EnvVarResolver(System::getenv)));
    }

    @VisibleForTesting
    YamlParser(YAMLMapper mapper, ParserFeatures parserFeatures, ValidationFeatures validationFeatures, PlaceholderResolver placeholderResolver){
        Objects.requireNonNull(parserFeatures);
        Objects.requireNonNull(validationFeatures);
        Objects.requireNonNull(placeholderResolver);
        this.mapper = Objects.requireNonNull(mapper);
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, parserFeatures, placeholderResolver);
        this.validator = ConfigurationValidator.from(validationFeatures);
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

    public <Y> Y convertTo(Class<Y> type) {
        Objects.requireNonNull(type);
        try {
            var yaml = mapper.treeToValue(root, type);
            if (yaml == null) {
                throw new YamlParsingException("Configuration could not be mapped to target class: " + type.getName());
            }
            validator.validate(yaml);
            return yaml;
        } catch (IOException | IllegalArgumentException exception) {
            throw new YamlParsingException(exception.getMessage(), exception);
        }
    }

    @Override
    public void close() {
        validator.close();
    }

}
