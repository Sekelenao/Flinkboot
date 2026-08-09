package io.github.sekelenao.flinkboot.core.api.typing.duration;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

public class DurationTypeInfoFactory extends TypeInfoFactory<Duration> {

    @Override
    public TypeInformation<Duration> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return DurationTypeInfo.INSTANCE;
    }

}
