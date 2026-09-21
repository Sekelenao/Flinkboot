package io.github.sekelenao.flinkboot.fluss.api.properties.sink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration properties for Apache Fluss sinks in Apache Flink.
 */
public final class FlussSinkProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<@NotBlank String> bootstrapServers;

    @NotBlank
    private final String database;

    @NotBlank
    private final String table;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code FlussSinkProperties} instance.
     *
     * @param name             sink operator name in Flink DAG
     * @param bootstrapServers list of Fluss coordinator/server addresses
     * @param database         target Fluss database name
     * @param table            target Fluss table name
     * @param properties       additional Fluss writer configuration properties
     */
    @JsonCreator
    public FlussSinkProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("database") String database,
        @JsonProperty("table") String table,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.database = database;
        this.table = table;
        this.properties = properties;
    }

    /**
     * Returns the sink operator name.
     *
     * @return the sink name
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
        if (!(other instanceof FlussSinkProperties)) {
            return false;
        }
        var o = (FlussSinkProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(database, o.database)
            && Objects.equals(table, o.table)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, database, table, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "FlussSinkProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", database='" + database + '\'' +
            ", table='" + table + '\'' +
            ", properties=" + properties +
            '}';
    }
}
