package io.github.sekelenao.flinkboot.fluss.api.properties.sink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.time.DurationMin;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Configuration properties for Apache Fluss sinks in Apache Flink.
 */
public class FlussSinkProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String database;

    @NotBlank
    private final String table;

    @PositiveOrZero
    private final Long batchSize;

    @DurationMin(millis = 0)
    private final Duration batchTimeout;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code FlussSinkProperties} instance.
     *
     * @param name             sink operator name in Flink DAG
     * @param bootstrapServers list of Fluss coordinator/server addresses
     * @param database         target Fluss database name
     * @param table            target Fluss table name
     * @param batchSize        writer bucket batch size in bytes
     * @param batchTimeout     writer bucket batch timeout duration
     * @param properties       additional Fluss writer configuration properties
     */
    @JsonCreator
    public FlussSinkProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("database") String database,
        @JsonProperty("table") String table,
        @JsonProperty("batch-size") Long batchSize,
        @JsonProperty("batch-timeout") Duration batchTimeout,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = Objects.requireNonNull(name);
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.database = Objects.requireNonNull(database);
        this.table = Objects.requireNonNull(table);
        this.batchSize = batchSize;
        this.batchTimeout = batchTimeout;
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
        if(bootstrapServers == null) {
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
     * Returns the writer bucket batch size in bytes, if configured.
     *
     * @return an {@link OptionalLong} containing the batch size in bytes, or empty if not set
     */
    public OptionalLong batchSize() {
        if (batchSize == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(batchSize);
    }

    /**
     * Returns the writer bucket batch timeout duration, if configured.
     *
     * @return an {@link Optional} containing the batch timeout duration, or empty if not set
     */
    public Optional<Duration> batchTimeout() {
        return Optional.ofNullable(batchTimeout);
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
            && Objects.equals(batchSize, o.batchSize)
            && Objects.equals(batchTimeout, o.batchTimeout)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, database, table, batchSize, batchTimeout, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "FlussSinkProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", database='" + database + '\'' +
            ", table='" + table + '\'' +
            ", batchSize=" + batchSize +
            ", batchTimeout=" + batchTimeout +
            ", properties=" + properties +
            '}';
    }
}

