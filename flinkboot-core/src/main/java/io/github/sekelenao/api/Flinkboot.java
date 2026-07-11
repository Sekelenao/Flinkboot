package io.github.sekelenao.api;

import io.github.sekelenao.internal.resource.Resource;
import io.github.sekelenao.internal.startup.StartupEnvironment;
import io.github.sekelenao.internal.yaml.YamlParser;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

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

}
