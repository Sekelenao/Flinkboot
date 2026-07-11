package io.github.sekelenao.flinkboot.core.api;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.internal.resource.Resource;
import io.github.sekelenao.flinkboot.core.internal.startup.StartupEnvironment;
import io.github.sekelenao.flinkboot.core.internal.yaml.YamlParser;

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

    public Optional<String> parameter(String parameter){
        Objects.requireNonNull(parameter);
        return startupEnvironment.get(parameter);
    }

    public <C> C configuration(Class<C> configurationClass) throws IOException {
        Objects.requireNonNull(configurationClass);
        var location = startupEnvironment.configurationResourceLocation();
        try(var parser = new YamlParser(); var inputStream = Resource.get(location).inputStream()) {
            return parser.parse(inputStream, configurationClass);
        }
    }

    public <C> C configuration(Class<C> configurationClass, Consumer<YAMLMapper.Builder> customizer) throws IOException {
        Objects.requireNonNull(configurationClass);
        Objects.requireNonNull(customizer);
        var location = startupEnvironment.configurationResourceLocation();
        try(var parser = new YamlParser(customizer); var inputStream = Resource.get(location).inputStream()) {
            return parser.parse(inputStream, configurationClass);
        }
    }

    public <C> C configuration(Class<C> configurationClass, YAMLMapper mapper) throws IOException {
        Objects.requireNonNull(configurationClass);
        Objects.requireNonNull(mapper);
        var location = startupEnvironment.configurationResourceLocation();
        try(var parser = new YamlParser(mapper); var inputStream = Resource.get(location).inputStream()) {
            return parser.parse(inputStream, configurationClass);
        }
    }

}
