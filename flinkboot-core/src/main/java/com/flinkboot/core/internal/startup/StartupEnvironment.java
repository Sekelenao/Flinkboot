package com.flinkboot.core.internal.startup;

import com.flinkboot.core.api.exception.parsing.IntegerParsingException;
import com.flinkboot.core.internal.annotation.VisibleForTesting;
import com.flinkboot.core.internal.parser.bool.StrictBooleanParser;
import com.flinkboot.core.internal.parser.integer.IntegerParser;
import com.flinkboot.core.internal.parser.yaml.ParserFeatures;
import com.flinkboot.core.internal.validation.ValidationFeatures;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StartupEnvironment {

    private static final String VIOLATIONS_LOG_SIZE = "flinkboot-configuration-violations-log-size";

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
        Objects.requireNonNull(key, "key must not be null");
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

    public ParserFeatures parserFeatures() {
        return ParserFeatures.builder()
            .permitOverride(flag("flinkboot-configuration-override"))
            .listMerging(flag("flinkboot-configuration-list-merging"))
            .build();
    }

    public ValidationFeatures validationFeatures() {
        var validationCapacity = get(VIOLATIONS_LOG_SIZE)
            .map(value -> IntegerParser.parseStrictlyPositive(value, () -> new IntegerParsingException(
                "Invalid value for '" + VIOLATIONS_LOG_SIZE
                    + "': must be a strictly positive integer, but was '" + value + "'"
            )))
            .orElse(10);
        return ValidationFeatures.builder()
            .disableValidation(flag("flinkboot-configuration-disable-validation"))
            .capacity(validationCapacity)
            .build();
    }

}
