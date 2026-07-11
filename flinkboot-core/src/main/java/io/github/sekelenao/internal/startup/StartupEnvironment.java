package io.github.sekelenao.internal.startup;

import java.util.Objects;

public final class StartupEnvironment {

    private final CommandLine commandLine;

    private final EnvVarResolver envVarResolver;
    
    public StartupEnvironment(String[] args){
        Objects.requireNonNull(args);
        this.commandLine = CommandLine.parse(args);
        this.envVarResolver = new EnvVarResolver(System::getenv);
    }

    /**
     * Testing-only constructor allowing injection of a controlled environment.
     * Not part of the public API.
     */
    StartupEnvironment(CommandLine commandLine, EnvVarResolver envVarResolver){
        this.commandLine = Objects.requireNonNull(commandLine);
        this.envVarResolver = Objects.requireNonNull(envVarResolver);
    }

    public String configurationResourceLocation(){
        var key = "flinkboot-configuration";
        return commandLine.option(key)
            .or(() -> envVarResolver.get(key))
            .orElse("file:job.yaml");
    }

}
