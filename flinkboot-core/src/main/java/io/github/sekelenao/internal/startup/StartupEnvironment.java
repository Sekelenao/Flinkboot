package io.github.sekelenao.internal.startup;

import java.util.Objects;

public final class StartupEnvironment {

    private final CommandLine commandLine;

    public StartupEnvironment(String[] args){
        Objects.requireNonNull(args);
        this.commandLine = CommandLine.parse(args);
    }

    public String configurationResourceLocation(){
        var key = "flinkboot-configuration";
        return commandLine.option(key)
            .or(() -> EnvVarResolver.get(key))
            .orElse("file:job.yaml");
    }

}
