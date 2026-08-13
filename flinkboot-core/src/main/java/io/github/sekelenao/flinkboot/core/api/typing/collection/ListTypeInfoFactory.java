package io.github.sekelenao.flinkboot.core.api.typing.collection;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ListTypeInfoFactory<E> extends TypeInfoFactory<List<E>> {

    @Override
    @SuppressWarnings("unchecked")
    public TypeInformation<List<E>> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        var elementType = genericParameters.get("E");
        if (elementType == null) {
            throw new InvalidTypesException("Type extraction is not possible on List (element type unknown).");
        }
        return Types.LIST((TypeInformation<E>) elementType);
    }

}
