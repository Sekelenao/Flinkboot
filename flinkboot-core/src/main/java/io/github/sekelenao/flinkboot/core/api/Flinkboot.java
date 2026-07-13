package io.github.sekelenao.flinkboot.core.api;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.internal.parser.YamlParser;
import io.github.sekelenao.flinkboot.core.internal.resource.Resource;
import io.github.sekelenao.flinkboot.core.internal.startup.StartupEnvironment;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class Flinkboot {

    private final StartupEnvironment startupEnvironment;

    private Flinkboot(String[] args) {
        this.startupEnvironment = new StartupEnvironment(args);
    }

    public static Flinkboot initialize(String[] args){
        Objects.requireNonNull(args);
        return new Flinkboot(args);
    }

    public boolean flag(String flag){
        Objects.requireNonNull(flag);
        return startupEnvironment.flag(flag);
    }

    public Optional<String> parameter(String parameter){
        Objects.requireNonNull(parameter);
        return startupEnvironment.get(parameter);
    }

    private <C> C readConfigurations(Class<C> configurationClass, YamlParser parser) throws IOException {
        var locations = startupEnvironment.configurationResourceLocations();
        for (var location : locations){
            try(var inputStream = Resource.get(location).inputStream()) {
                parser.parse(inputStream);
            }
        }
        return parser.convertTo(configurationClass);
    }

    public <C> C configuration(Class<C> configurationClass) throws IOException {
        Objects.requireNonNull(configurationClass);
        try(var parser = new YamlParser(startupEnvironment.configurationMergeFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

    public <C> C configuration(Class<C> configurationClass, Consumer<YAMLMapper.Builder> customizer) throws IOException {
        Objects.requireNonNull(configurationClass);
        Objects.requireNonNull(customizer);
        try(var parser = new YamlParser(customizer, startupEnvironment.configurationMergeFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

    public <C> C configuration(Class<C> configurationClass, YAMLMapper mapper) throws IOException {
        Objects.requireNonNull(configurationClass);
        Objects.requireNonNull(mapper);
        try(var parser = new YamlParser(mapper, startupEnvironment.configurationMergeFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

}
