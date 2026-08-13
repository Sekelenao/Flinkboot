package io.github.sekelenao.flinkboot.core.api.typing.collection;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.Map;

public class MapTypeInfoFactory<K, V> extends TypeInfoFactory<Map<K, V>> {

    @Override
    @SuppressWarnings("unchecked")
    public TypeInformation<Map<K, V>> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        var keyType = genericParameters.get("K");
        var valueType = genericParameters.get("V");
        if (keyType == null || valueType == null) {
            throw new InvalidTypesException("Type extraction is not possible on Map (key or value type unknown).");
        }
        return Types.MAP((TypeInformation<K>) keyType, (TypeInformation<V>) valueType);
    }

}
