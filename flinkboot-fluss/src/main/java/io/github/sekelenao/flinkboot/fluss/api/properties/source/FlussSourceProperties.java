package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import io.github.sekelenao.flinkboot.fluss.api.exception.InvalidFlussSourcePropertiesException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Configuration properties for Apache Fluss sources in Apache Flink.
 */
public class FlussSourceProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String database;

    @NotBlank
    private final String table;

    @NotNull
    private final FlussStartupMode startupMode;

    @PositiveOrZero
    private final Long startupTimestamp;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code FlussSourceProperties} instance.
     *
     * @param name             source operator name in Flink DAG
     * @param bootstrapServers list of Fluss coordinator/server addresses
     * @param database         target Fluss database name
     * @param table            target Fluss table name
     * @param startupMode      startup mode (EARLIEST, LATEST, TIMESTAMP)
     * @param startupTimestamp timestamp in milliseconds (required if startupMode is TIMESTAMP)
     * @param properties       additional Fluss client/scanner configuration properties
     * @throws InvalidFlussSourcePropertiesException if startup mode is TIMESTAMP but startupTimestamp is missing
     */
    @JsonCreator
    public FlussSourceProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("database") String database,
        @JsonProperty("table") String table,
        @JsonProperty("startup-mode") FlussStartupMode startupMode,
        @JsonProperty("startup-timestamp") Long startupTimestamp,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.database = database;
        this.table = table;
        this.startupMode = startupMode;
        this.startupTimestamp = startupTimestamp;
        this.properties = properties;
        validate();
    }

    private void validate() {
        if (startupMode == null) {
            return;
        }
        if (startupMode == FlussStartupMode.TIMESTAMP) {
            if (startupTimestamp == null) {
                throw new InvalidFlussSourcePropertiesException("startup-timestamp is required when startup-mode is TIMESTAMP");
            }
        } else if (startupTimestamp != null) {
            throw new InvalidFlussSourcePropertiesException("startup-timestamp must not be specified when startup-mode is " + startupMode);
        }
    }

    /**
     * Returns the source operator name.
     *
     * @return the source name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the list of Fluss bootstrap server addresses.
     *
     * @return the bootstrap servers list
     */
    public List<String> bootstrapServers() {
        if (bootstrapServers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bootstrapServers);
    }

    /**
     * Returns the Fluss database name.
     *
     * @return the database name
     */
    public String database() {
        return database;
    }

    /**
     * Returns the Fluss table name.
     *
     * @return the table name
     */
    public String table() {
        return table;
    }

    /**
     * Returns the Fluss startup mode.
     *
     * @return the startup mode
     */
    public FlussStartupMode startupMode() {
        return startupMode;
    }

    /**
     * Returns the startup timestamp in milliseconds, if configured.
     *
     * @return an {@link OptionalLong} containing the timestamp, or empty if not set
     */
    public OptionalLong startupTimestamp() {
        if (startupTimestamp == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(startupTimestamp);
    }

    /**
     * Returns additional Fluss configuration properties.
     *
     * @return an unmodifiable map of properties
     */
    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FlussSourceProperties)) {
            return false;
        }
        var o = (FlussSourceProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(database, o.database)
            && Objects.equals(table, o.table)
            && startupMode == o.startupMode
            && Objects.equals(startupTimestamp, o.startupTimestamp)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, database, table, startupMode, startupTimestamp, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "FlussSourceProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", database='" + database + '\'' +
            ", table='" + table + '\'' +
            ", startupMode=" + startupMode +
            ", startupTimestamp=" + startupTimestamp +
            ", properties=" + properties +
            '}';
    }
}
