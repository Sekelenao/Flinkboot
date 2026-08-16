package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.Map;

/**
 * {@link TypeInfoFactory} for {@link LocalTime} fields in Flink POJOs.
 * <p>
 * Annotate {@link LocalTime} fields with {@code @TypeInfo(LocalTimeTypeInfoFactory.class)} to enable
 * native Flink serialization without slow Kryo fallback.
 */
public class LocalTimeTypeInfoFactory extends TypeInfoFactory<LocalTime> {

    @Override
    public TypeInformation<LocalTime> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.LOCAL_TIME;
    }

}
