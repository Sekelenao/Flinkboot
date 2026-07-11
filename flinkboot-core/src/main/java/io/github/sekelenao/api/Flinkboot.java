package io.github.sekelenao.api;

import io.github.sekelenao.internal.resource.Resource;
import io.github.sekelenao.internal.startup.StartupEnvironment;
import io.github.sekelenao.internal.yaml.YamlParser;

import java.io.IOException;
import java.util.Objects;

public final class Flinkboot {

    private final StartupEnvironment startupEnvironment;

    private Flinkboot(String[] args) {
        this.startupEnvironment = new StartupEnvironment(args);
    }

    public static Flinkboot initialize(String[] args){
        Objects.requireNonNull(args);
        return new Flinkboot(args);
    }

    public <C> C loadConfiguration(Class<C> configurationClass) throws IOException {
        var location = startupEnvironment.configurationResourceLocation();
        try(var parser = new YamlParser()) {
            var resource = Resource.get(location);
            return parser.parse(resource.inputStream(), configurationClass);
        }
    }

}
