package io.github.sekelenao.flinkboot.core.api;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;
import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.resource.Resource;
import io.github.sekelenao.flinkboot.core.internal.execution.ExecutionEnvironmentFactory;
import io.github.sekelenao.flinkboot.core.internal.parser.yaml.YamlParser;
import io.github.sekelenao.flinkboot.core.internal.startup.StartupEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Main entrypoint for bootstrapping Apache Flink applications with Flinkboot.
 * <p>
 * {@code Flinkboot} simplifies Flink job lifecycle management by providing:
 * <ul>
 *   <li>Multi-source configuration loading (YAML files, CLI arguments, environment variables)</li>
 *   <li>Declarative {@link StreamExecutionEnvironment} instantiation from structured properties</li>
 *   <li>Unified flag and parameter resolution</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyJob {
 *     public static void main(String[] args) throws Exception {
 *         Flinkboot boot = Flinkboot.initialize(args);
 *
 *         // 1. Load and validate YAML configurations
 *         MyJobConfig config = boot.configuration(MyJobConfig.class);
 *
 *         // 2. Instantiate StreamExecutionEnvironment
 *         StreamExecutionEnvironment env = boot.executionEnvironment(config.job());
 *
 *         // 3. Define pipeline and execute
 *         env.fromElements("Hello", "Flinkboot").print();
 *         env.execute(config.job().name());
 *     }
 * }
 * }</pre>
 *
 * @see Resource
 * @see JobProperties
 */
public final class Flinkboot {

    private final StartupEnvironment startupEnvironment;

    private Flinkboot(String[] args) {
        this.startupEnvironment = new StartupEnvironment(args);
    }

    /**
     * Initializes a new {@code Flinkboot} instance from command-line arguments.
     *
     * @param args the command-line arguments passed to the main method (must not be {@code null})
     * @return a configured {@code Flinkboot} instance
     * @throws NullPointerException if {@code args} is {@code null}
     */
    public static Flinkboot initialize(String[] args){
        Objects.requireNonNull(args, "args must not be null");
        return new Flinkboot(args);
    }

    /**
     * Resolves the boolean value of a flag from command-line arguments (e.g. {@code --my-flag})
     * or environment variables (e.g. {@code MY_FLAG=true}).
     *
     * @param flag the flag name without leading dashes (must not be {@code null})
     * @return {@code true} if the flag is enabled; {@code false} otherwise
     * @throws NullPointerException if {@code flag} is {@code null}
     */
    public boolean flag(String flag){
        Objects.requireNonNull(flag, "flag must not be null");
        return startupEnvironment.flag(flag);
    }

    /**
     * Resolves a string parameter value from command-line arguments (e.g. {@code -my-param value})
     * or environment variables (e.g. {@code MY_PARAM=value}).
     *
     * @param parameter the parameter name without leading dashes (must not be {@code null})
     * @return an {@link Optional} containing the parameter value if present, or {@link Optional#empty()}
     * @throws NullPointerException if {@code parameter} is {@code null}
     */
    public Optional<String> parameter(String parameter){
        Objects.requireNonNull(parameter, "parameter must not be null");
        return startupEnvironment.get(parameter);
    }

    private <C> C readConfigurations(Class<C> configurationClass, YamlParser parser) throws IOException {
        var locations = startupEnvironment.configurationResourceLocations();
        for (var location : locations){
            try(var inputStream = Resource.of(location).inputStream()) {
                parser.parse(inputStream);
            }
        }
        return parser.convertTo(configurationClass);
    }

    /**
     * Loads, merges, and validates YAML configuration files into an instance of the target class.
     * <p>
     * Configuration file locations default to {@code "classpath:job-configuration.yaml"} and can be overridden
     * via the {@code -flinkboot-configurations} CLI argument or {@code FLINKBOOT_CONFIGURATIONS} environment variable.
     *
     * @param configurationClass the target class or record to deserialize into (must not be {@code null})
     * @param <C>                the configuration type
     * @return the deserialized and validated configuration instance
     * @throws IOException                      if an I/O error occurs while reading configuration files
     * @throws NullPointerException            if {@code configurationClass} is {@code null}
     * @throws YamlParsingException             if YAML syntax or deserialization mapping fails
     * @throws ConfigurationValidationException if bean validation constraints are violated
     */
    public <C> C configuration(Class<C> configurationClass) throws IOException {
        Objects.requireNonNull(configurationClass, "configurationClass must not be null");
        try(var parser = new YamlParser(startupEnvironment.parserFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

    /**
     * Loads, merges, and validates YAML configuration files using a customized Jackson YAML builder.
     *
     * @param configurationClass the target class or record to deserialize into (must not be {@code null})
     * @param customizer         a consumer allowing custom Jackson {@link YAMLMapper.Builder} configuration (must not be {@code null})
     * @param <C>                the configuration type
     * @return the deserialized and validated configuration instance
     * @throws IOException                      if an I/O error occurs while reading configuration files
     * @throws NullPointerException            if {@code configurationClass} or {@code customizer} is {@code null}
     * @throws YamlParsingException             if YAML syntax or deserialization mapping fails
     * @throws ConfigurationValidationException if bean validation constraints are violated
     */
    public <C> C configuration(Class<C> configurationClass, Consumer<YAMLMapper.Builder> customizer) throws IOException {
        Objects.requireNonNull(configurationClass, "configurationClass must not be null");
        Objects.requireNonNull(customizer, "customizer must not be null");
        try(var parser = new YamlParser(customizer, startupEnvironment.parserFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

    /**
     * Loads, merges, and validates YAML configuration files using a pre-configured {@link YAMLMapper}.
     *
     * @param configurationClass the target class or record to deserialize into (must not be {@code null})
     * @param mapper             the pre-configured YAML mapper (must not be {@code null})
     * @param <C>                the configuration type
     * @return the deserialized and validated configuration instance
     * @throws IOException                      if an I/O error occurs while reading configuration files
     * @throws NullPointerException            if {@code configurationClass} or {@code mapper} is {@code null}
     * @throws YamlParsingException             if YAML syntax or deserialization mapping fails
     * @throws ConfigurationValidationException if bean validation constraints are violated
     */
    public <C> C configuration(Class<C> configurationClass, YAMLMapper mapper) throws IOException {
        Objects.requireNonNull(configurationClass, "configurationClass must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");
        try(var parser = new YamlParser(mapper, startupEnvironment.parserFeatures())) {
            return readConfigurations(configurationClass, parser);
        }
    }

    /**
     * Creates and configures a Flink {@link StreamExecutionEnvironment} based on declarative {@link JobProperties}.
     * <p>
     * Configures checkpointing, RocksDB state backends, restart strategies, savepoints, execution modes,
     * and local Web UI if specified.
     *
     * @param jobProperties the declarative job properties (must not be {@code null})
     * @return a fully configured {@link StreamExecutionEnvironment}
     * @throws NullPointerException if {@code jobProperties} is {@code null}
     */
    public StreamExecutionEnvironment executionEnvironment(JobProperties jobProperties) {
        Objects.requireNonNull(jobProperties, "jobProperties must not be null");
        return new ExecutionEnvironmentFactory().create(jobProperties);
    }

}
