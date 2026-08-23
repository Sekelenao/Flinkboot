package io.github.sekelenao.flinkboot.core.internal.startup;

import io.github.sekelenao.flinkboot.core.internal.annotation.VisibleForTesting;
import io.github.sekelenao.flinkboot.core.internal.parser.bool.StrictBooleanParser;
import io.github.sekelenao.flinkboot.core.internal.parser.yaml.ParserFeatures;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public boolean flag(String flag){
        Objects.requireNonNull(flag);
        return commandLine.flag(flag) || envVarResolver.get(flag).map(StrictBooleanParser::parse).orElse(Boolean.FALSE);
    }

    public Optional<String> get(String key){
        return commandLine.option(key).or(() -> envVarResolver.get(key));
    }

    public List<String> configurationResourceLocations(){
        return get("flinkboot-configurations")
            .map(value -> Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableList()))
            .orElse(Collections.singletonList("classpath:job-configuration.yaml"));
    }

    public ParserFeatures parserFeatures(){
        var validationCapacity = get("flinkboot-configuration-violations-log-size")
            .map(Integer::parseInt)
            .filter(size -> size > 0)
            .orElse(10);
        return ParserFeatures.builder()
            .permitOverride(flag("flinkboot-configuration-override"))
            .listMerging(flag("flinkboot-configuration-list-merging"))
            .validationCapacity(validationCapacity)
            .build();
    }

}
