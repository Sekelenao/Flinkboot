package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

/**
 * {@link TypeInfoFactory} for {@link Duration} fields in Flink POJOs.
 * <p>
 * Annotate {@link Duration} fields with {@code @TypeInfo(DurationTypeInfoFactory.class)} to enable
 * native Flink serialization without slow Kryo fallback.
 *
 * <h3>Example Usage</h3>
 * <pre>
 * public class Event {
 *     &#64;TypeInfo(DurationTypeInfoFactory.class)
 *     public Duration latency;
 * }
 * </pre>
 */
public class DurationTypeInfoFactory extends TypeInfoFactory<Duration> {

    @Override
    public TypeInformation<Duration> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return DurationTypeInfo.INSTANCE;
    }

}
