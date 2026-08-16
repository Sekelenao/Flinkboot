package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * {@link TypeInfoFactory} for {@link LocalDateTime} fields in Flink POJOs.
 * <p>
 * Annotate {@link LocalDateTime} fields with {@code @TypeInfo(LocalDateTimeTypeInfoFactory.class)} to enable
 * native Flink serialization without slow Kryo fallback.
 */
public class LocalDateTimeTypeInfoFactory extends TypeInfoFactory<LocalDateTime> {

    @Override
    public TypeInformation<LocalDateTime> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.LOCAL_DATE_TIME;
    }

}
