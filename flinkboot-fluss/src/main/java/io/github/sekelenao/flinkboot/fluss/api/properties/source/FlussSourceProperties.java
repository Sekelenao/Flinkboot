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
        this.name = Objects.requireNonNull(name);
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.database = Objects.requireNonNull(database);
        this.table = Objects.requireNonNull(table);
        this.startupMode = Objects.requireNonNull(startupMode);
        this.startupTimestamp = startupTimestamp;
        this.properties = properties;
        validate();
    }

    private void validate() {
        if (startupMode == FlussStartupMode.TIMESTAMP && startupTimestamp == null) {
            throw new InvalidFlussSourcePropertiesException("startupTimestamp must not be null when startupMode is TIMESTAMP");
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
        return startupTimestamp == null ? OptionalLong.empty() : OptionalLong.of(startupTimestamp);
    }

    /**
     * Returns additional Fluss configuration properties.
     *
     * @return an unmodifiable map of properties
     */
    public Map<String, String> properties() {
        return properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (FlussSourceProperties) o;
        return Objects.equals(name, that.name)
            && Objects.equals(bootstrapServers, that.bootstrapServers)
            && Objects.equals(database, that.database)
            && Objects.equals(table, that.table)
            && startupMode == that.startupMode
            && Objects.equals(startupTimestamp, that.startupTimestamp)
            && Objects.equals(properties, that.properties);
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
