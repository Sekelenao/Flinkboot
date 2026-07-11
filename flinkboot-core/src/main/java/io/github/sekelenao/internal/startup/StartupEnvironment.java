package io.github.sekelenao.internal.startup;

import io.github.sekelenao.internal.annotation.VisibleForTesting;

import java.util.Objects;

public final class StartupEnvironment {

    private final CommandLine commandLine;

    private final EnvVarResolver envVarResolver;
    
    public StartupEnvironment(String[] args){
        Objects.requireNonNull(args);
        this.commandLine = CommandLine.parse(args);
        this.envVarResolver = new EnvVarResolver(System::getenv);
    }

    @VisibleForTesting
    StartupEnvironment(CommandLine commandLine, EnvVarResolver envVarResolver){
        this.commandLine = Objects.requireNonNull(commandLine);
        this.envVarResolver = Objects.requireNonNull(envVarResolver);
    }

    public String configurationResourceLocation(){
        var key = "flinkboot-configuration";
        return commandLine.option(key)
            .or(() -> envVarResolver.get(key))
            .orElse("file:job-configuration.yaml");
    }

}
