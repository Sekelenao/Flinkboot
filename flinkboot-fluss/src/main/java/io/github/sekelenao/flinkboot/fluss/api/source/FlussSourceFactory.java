package io.github.sekelenao.flinkboot.fluss.api.source;

import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.apache.fluss.config.Configuration;
import org.apache.fluss.flink.source.FlussSource;
import org.apache.fluss.flink.source.FlussSourceBuilder;
import org.apache.fluss.flink.source.deserializer.FlussDeserializationSchema;

import java.util.Objects;

/**
 * Factory utility for constructing Apache Flink {@link FlussSource} instances from configuration properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * FlussSource<RowData> source = FlussSourceFactory.supplyFor(
 *     config.flussSource(),
 *     deserializationSchema
 * );
 *
 * DataStream<RowData> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), config.flussSource().name());
 * }</pre>
 */
public final class FlussSourceFactory {

    private FlussSourceFactory() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates and pre-configures a {@link FlussSourceBuilder} from configuration properties.
     *
     * @param config                the Fluss source configuration properties
     * @param deserializationSchema the deserialization schema to decode Fluss records
     * @param <T>                   the deserialized record type
     * @return a pre-configured {@link FlussSourceBuilder}
     * @throws NullPointerException if {@code config} or {@code deserializationSchema} is {@code null}
     */
    public static <T> FlussSourceBuilder<T> supplyBuilderFor(
        FlussSourceProperties config,
        FlussDeserializationSchema<T> deserializationSchema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(deserializationSchema, "deserializationSchema must not be null");

        var flussConfig = new Configuration();
        config.properties().forEach(flussConfig::setString);

        var builder = new FlussSourceBuilder<T>()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setDatabase(config.database())
            .setTable(config.table())
            .setFlussConfig(flussConfig)
            .setDeserializationSchema(deserializationSchema);

        if (config.startupMode() == FlussStartupMode.TIMESTAMP) {
            builder.setStartingOffsets(FlussStartupMode.fromTimestamp(config.startupTimestamp().orElseThrow()));
        } else if (config.startupMode().offsetsInitializer() != null) {
            builder.setStartingOffsets(config.startupMode().offsetsInitializer());
        }

        return builder;
    }

    /**
     * Creates and builds a {@link FlussSource} from configuration properties.
     *
     * @param config                the Fluss source configuration properties
     * @param deserializationSchema the deserialization schema to decode Fluss records
     * @param <T>                   the deserialized record type
     * @return a built and immutable {@link FlussSource}
     * @throws NullPointerException if {@code config} or {@code deserializationSchema} is {@code null}
     */
    public static <T> FlussSource<T> supplyFor(
        FlussSourceProperties config,
        FlussDeserializationSchema<T> deserializationSchema
    ) {
        return supplyBuilderFor(config, deserializationSchema).build();
    }
}
