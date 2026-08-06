package io.github.sekelenao.flinkboot.core.api.typing;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;

public class InstantTypeInfoFactory extends TypeInfoFactory<Instant> {

    @Override
    public TypeInformation<Instant> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.INSTANT;
    }

}
