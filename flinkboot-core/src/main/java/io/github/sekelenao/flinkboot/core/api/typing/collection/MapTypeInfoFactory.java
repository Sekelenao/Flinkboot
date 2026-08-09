package io.github.sekelenao.flinkboot.core.api.typing.collection;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.Map;

public class MapTypeInfoFactory extends TypeInfoFactory<Map<?, ?>> {

    @SuppressWarnings("unchecked")
    @Override
    public TypeInformation<Map<?, ?>> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        TypeInformation<?> keyType = genericParameters.get("K");
        TypeInformation<?> valueType = genericParameters.get("V");

        if (keyType == null || valueType == null) {
            throw new InvalidTypesException("Type extraction is not possible on Map (key or value type unknown).");
        }

        return (TypeInformation<Map<?, ?>>) (TypeInformation<?>) Types.MAP(keyType, valueType);
    }
}
