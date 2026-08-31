package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;
import io.github.sekelenao.flinkboot.core.internal.annotation.VisibleForTesting;
import io.github.sekelenao.flinkboot.core.internal.startup.EnvVarResolver;
import io.github.sekelenao.flinkboot.core.internal.validation.ConfigurationValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

public final class YamlParser implements AutoCloseable {

    private final ConfigurationValidator validator;

    private final YAMLMapper mapper;

    private final JsonNode root;

    private final MergeProcessor mergeProcessor;

    public YamlParser(ParserFeatures features) {
        this(additionalConfiguration -> {}, Objects.requireNonNull(features));
    }

    public YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures features) {
        this(additionalConfiguration, features, new PlaceholderResolver(new EnvVarResolver(System::getenv)));
    }

    @VisibleForTesting
    YamlParser(Consumer<YAMLMapper.Builder> additionalConfiguration, ParserFeatures features, PlaceholderResolver placeholderResolver) {
        Objects.requireNonNull(additionalConfiguration);
        Objects.requireNonNull(features);
        Objects.requireNonNull(placeholderResolver);
        var builder = YAMLMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .addModule(new JavaTimeModule())
            .findAndAddModules();
        additionalConfiguration.accept(builder);
        this.mapper = builder.build();
        this.root = mapper.createObjectNode();
        this.mergeProcessor = new MergeProcessor((ObjectNode) root, features, placeholderResolver);
        this.validator = new ConfigurationValidator(features.validationCapacity());
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
        this.validator = new ConfigurationValidator(parserFeatures.validationCapacity());
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
                throw new YamlParsingException(
                    "Configuration could not be mapped to target class: "
                        + type.getSimpleName()
                );
            }
            validator.validate(yaml);
            return yaml;
        } catch (IOException | IllegalArgumentException exception) {
            throw new YamlParsingException(
                exception.getMessage(),
                exception
            );
        }
    }

    @Override
    public void close() {
        validator.close();
    }

}
