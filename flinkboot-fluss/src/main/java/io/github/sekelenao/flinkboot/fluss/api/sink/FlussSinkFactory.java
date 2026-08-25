package io.github.sekelenao.flinkboot.fluss.api.sink;

import io.github.sekelenao.flinkboot.core.internal.time.DurationFormatter;
import io.github.sekelenao.flinkboot.fluss.api.properties.sink.FlussSinkProperties;
import org.apache.fluss.flink.sink.FlussSink;
import org.apache.fluss.flink.sink.FlussSinkBuilder;
import org.apache.fluss.flink.sink.serializer.FlussSerializationSchema;

import java.util.HashMap;
import java.util.Objects;

/**
 * Factory utility for constructing Apache Flink {@link FlussSink} instances from configuration properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * FlussSink<RowData> sink = FlussSinkFactory.supplyFor(
 *     config.flussSink(),
 *     serializationSchema
 * );
 *
 * stream.sinkTo(sink).name(config.flussSink().name());
 * }</pre>
 */
public final class FlussSinkFactory {

    private FlussSinkFactory() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates and pre-configures a {@link FlussSinkBuilder} from configuration properties.
     *
     * @param config              the Fluss sink configuration properties
     * @param serializationSchema the serialization schema to encode records for Fluss
     * @param <T>                 the input record type
     * @return a pre-configured {@link FlussSinkBuilder}
     * @throws NullPointerException if {@code config} or {@code serializationSchema} is {@code null}
     */
    public static <T> FlussSinkBuilder<T> supplyBuilderFor(
        FlussSinkProperties config,
        FlussSerializationSchema<T> serializationSchema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(serializationSchema);

        var options = new HashMap<>(config.properties());
        config.batchSize().ifPresent(batchSize -> options.put("client.writer.bucket.batch.size", String.valueOf(batchSize)));
        config.batchTimeout().ifPresent(timeout -> options.put("client.writer.bucket.batch.timeout", DurationFormatter.format(timeout)));

        return new FlussSinkBuilder<T>()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setDatabase(config.database())
            .setTable(config.table())
            .setOptions(options)
            .setSerializationSchema(serializationSchema);
    }

    /**
     * Creates and builds a {@link FlussSink} from configuration properties.
     *
     * @param config              the Fluss sink configuration properties
     * @param serializationSchema the serialization schema to encode records for Fluss
     * @param <T>                 the input record type
     * @return a built and immutable {@link FlussSink}
     * @throws NullPointerException if {@code config} or {@code serializationSchema} is {@code null}
     */
    public static <T> FlussSink<T> supplyFor(
        FlussSinkProperties config,
        FlussSerializationSchema<T> serializationSchema
    ) {
        return supplyBuilderFor(config, serializationSchema).build();
    }
}
