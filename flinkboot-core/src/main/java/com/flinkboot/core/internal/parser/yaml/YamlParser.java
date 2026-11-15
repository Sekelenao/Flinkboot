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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

public final class YamlParser {

    private final YAMLMapper mapper;

    private final JsonNode root;

    private final MergeProcessor mergeProcessor;

    public YamlParser(ParserFeatures parserFeatures) {
        this(additionalConfiguration -> {}, Objects.requireNonNull(parserFeatures));
    }

    public YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures parserFeatures) {
        this(additionalConfiguration, parserFeatures, new PlaceholderResolver(new EnvVarResolver(System::getenv)));
    }

    @VisibleForTesting
    YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures parserFeatures, PlaceholderResolver placeholderResolver) {
        Objects.requireNonNull(additionalConfiguration);
        Objects.requireNonNull(parserFeatures);
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
    }

    public YamlParser(YAMLMapper mapper, ParserFeatures parserFeatures){
        this(mapper, parserFeatures, new PlaceholderResolver(new EnvVarResolver(System::getenv)));
    }

    @VisibleForTesting
    YamlParser(YAMLMapper mapper, ParserFeatures parserFeatures, PlaceholderResolver placeholderResolver){
        Objects.requireNonNull(parserFeatures);
        Objects.requireNonNull(placeholderResolver);
        this.mapper = Objects.requireNonNull(mapper);
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, parserFeatures, placeholderResolver);
    }

    public void parse(InputStream source){
        Objects.requireNonNull(source);
        try (var parser = mapper.getFactory().createParser(source);
             var nodes = mapper.readValues(parser, JsonNode.class)) {
            while (nodes.hasNextValue()) {
                var node = nodes.nextValue();
                if (node == null || node.isNull() || node.isMissingNode()) {
                    continue;
                }
                if (!node.isObject()) {
                    throw new YamlParsingException("Configuration source is invalid");
                }
                mergeProcessor.apply((ObjectNode) node);
            }
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
            return yaml;
        } catch (IOException | IllegalArgumentException exception) {
            throw new YamlParsingException(exception.getMessage(), exception);
        }
    }

}
