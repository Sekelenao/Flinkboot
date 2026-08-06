package io.github.sekelenao.flinkboot.core.api.typing;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;

public class LocalDateTypeInfoFactory extends TypeInfoFactory<LocalDate> {

    @Override
    public TypeInformation<LocalDate> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.LOCAL_DATE;
    }

}
