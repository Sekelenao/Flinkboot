package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;

/**
 * {@link TypeInfoFactory} for {@link LocalDate} fields in Flink POJOs.
 * <p>
 * Annotate {@link LocalDate} fields with {@code @TypeInfo(LocalDateTypeInfoFactory.class)} to enable
 * native Flink serialization without slow Kryo fallback.
 */
public class LocalDateTypeInfoFactory extends TypeInfoFactory<LocalDate> {

    @Override
    public TypeInformation<LocalDate> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.LOCAL_DATE;
    }

}
